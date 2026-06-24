package com.quoteflow.service;

import com.quoteflow.dto.demande.DemandeRequest;
import com.quoteflow.dto.demande.DemandeResponse;
import com.quoteflow.entity.DemandeDevis;
import com.quoteflow.entity.PieceJointe;
import com.quoteflow.entity.User;
import com.quoteflow.entity.enums.Role;
import com.quoteflow.entity.enums.StatutDemande;
import com.quoteflow.exception.BusinessException;
import com.quoteflow.exception.ResourceNotFoundException;
import com.quoteflow.repository.DemandeDevisRepository;
import com.quoteflow.repository.PieceJointeRepository;
import com.quoteflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Year;
import java.util.List;
import java.util.UUID;

 // cycle de vie des demandes de devis.
 // Soumission (CLIENT), attribution (ADMIN), consultation, suivi des statuts.

@Service
public class DemandeService {

    private final DemandeDevisRepository demandeRepository;
    private final UserRepository userRepository;
    private final WebhookService webhookService;
    private final NotificationService notificationService;
    private final PieceJointeRepository pieceJointeRepository;
    private final com.quoteflow.repository.DevisRepository devisRepository;
    private final String attachmentsDir;
    private final String adminEmail;

    public DemandeService(DemandeDevisRepository demandeRepository, UserRepository userRepository,
                          WebhookService webhookService, NotificationService notificationService,
                          PieceJointeRepository pieceJointeRepository,
                          com.quoteflow.repository.DevisRepository devisRepository,
                          @Value("${app.upload.attachments-dir}") String attachmentsDir,
                          @Value("${app.admin.email}") String adminEmail) {
        this.demandeRepository = demandeRepository;
        this.userRepository = userRepository;
        this.webhookService = webhookService;
        this.notificationService = notificationService;
        this.pieceJointeRepository = pieceJointeRepository;
        this.devisRepository = devisRepository;
        this.attachmentsDir = attachmentsDir;
        this.adminEmail = adminEmail;
    }

    // Le client soumet une demande -> statut EN_ATTENTE + email de confirmation 
    @Transactional
    public DemandeResponse soumettre(Long clientId, DemandeRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable"));

        DemandeDevis demande = DemandeDevis.builder()
                .description(request.getDescription())
                .budget(request.getBudget())
                .statut(StatutDemande.EN_ATTENTE)
                .client(client)
                .build();

        demande = demandeRepository.save(demande);
        // Reference lisible apres obtention de l'id auto-genere.
        demande.setReference(genererReference(demande.getId()));
        demande = demandeRepository.save(demande);

        webhookService.notifierConfirmationDemande(client.getEmail(), client.getNom(), demande.getReference());
        notificationService.tracer("CONFIRMATION_DEMANDE",
                "Confirmation de la demande " + demande.getReference(), client, demande, true);

        return toResponse(demande);
    }

    // L'admin attribue la demande a un employe -> statut EN_COURS. 

    @Transactional
    public DemandeResponse attribuer(Long demandeId, Long employeId) {
        DemandeDevis demande = getEntity(demandeId);
        User employe = userRepository.findById(employeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employe introuvable"));
        if (employe.getRole() != Role.EMPLOYE) {
            throw new BusinessException("La demande ne peut etre attribuee qu'a un employe");
        }
        if (demande.getStatut() == StatutDemande.VALIDE || demande.getStatut() == StatutDemande.REFUSE) {
            throw new BusinessException("Une demande cloturee ne peut plus etre attribuee");
        }
        demande.setEmploye(employe);
        demande.setStatut(StatutDemande.EN_COURS);
        demande = demandeRepository.save(demande);

        webhookService.notifierChangementStatut(demande.getClient().getEmail(),
                demande.getClient().getNom(), demande.getReference(), StatutDemande.EN_COURS.name());
        notificationService.tracer("CHANGEMENT_STATUT",
                "Demande prise en charge (EN_COURS)", demande.getClient(), demande, true);

        return toResponse(demande);
    }

    // Refus par le client ou l'admin -> statut REFUSE. 
    @Transactional
    public DemandeResponse refuser(Long demandeId) {
        DemandeDevis demande = getEntity(demandeId);
        demande.setStatut(StatutDemande.REFUSE);
        demande = demandeRepository.save(demande);
        webhookService.notifierChangementStatut(demande.getClient().getEmail(),
                demande.getClient().getNom(), demande.getReference(), StatutDemande.REFUSE.name());
        notificationService.tracer("CHANGEMENT_STATUT", "Demande refusee",
                demande.getClient(), demande, true);
        return toResponse(demande);
    }

    public List<DemandeResponse> mesDemandes(Long clientId) {
        return demandeRepository.findByClientId(clientId).stream().map(this::toResponse).toList();
    }

    public List<DemandeResponse> toutesLesDemandes() {
        return demandeRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DemandeResponse detail(Long id) {
        return toResponse(getEntity(id));
    }

    //Ajoute une ou plusieurs pieces jointes PDF a une demande.
    //Seul le client proprietaire de la demande peut y joindre des fichiers.
    
    @Transactional
    public DemandeResponse ajouterPiecesJointes(Long demandeId, Long clientId, MultipartFile[] fichiers) {
        DemandeDevis demande = getEntity(demandeId);

        if (!demande.getClient().getId().equals(clientId)) {
            throw new BusinessException("Cette demande ne vous appartient pas");
        }
        if (fichiers == null || fichiers.length == 0) {
            throw new BusinessException("Aucun fichier fourni");
        }

        try {
            Files.createDirectories(Paths.get(attachmentsDir));
            for (MultipartFile fichier : fichiers) {
                if (fichier.isEmpty()) continue;

                // On n'accepte que des PDF.
                String nom = fichier.getOriginalFilename() != null ? fichier.getOriginalFilename() : "fichier.pdf";
                boolean estPdf = "application/pdf".equalsIgnoreCase(fichier.getContentType())
                        || nom.toLowerCase().endsWith(".pdf");
                if (!estPdf) {
                    throw new BusinessException("Seuls les fichiers PDF sont acceptes (" + nom + ")");
                }

                // Nom de stockage unique pour eviter les collisions.
                String nomStockage = UUID.randomUUID() + "-" + nom.replaceAll("[^a-zA-Z0-9._-]", "_");
                Path cible = Paths.get(attachmentsDir, nomStockage);
                fichier.transferTo(cible.toAbsolutePath());

                PieceJointe pj = PieceJointe.builder()
                        .nomOriginal(nom)
                        .cheminFichier(cible.toString())
                        .typeMime("application/pdf")
                        .tailleOctets(fichier.getSize())
                        .demande(demande)
                        .build();
                demande.getPiecesJointes().add(pj);
            }
            // Cascade ALL : la sauvegarde de la demande persiste les pieces jointes.
            demande = demandeRepository.save(demande);
        } catch (IOException e) {
            throw new BusinessException("Erreur lors de l'enregistrement du fichier : " + e.getMessage());
        }
        return toResponse(demande);
    }

    // Recupere une piece jointe pour telechargement. 
    public PieceJointe getPieceJointe(Long pieceJointeId) {
        return pieceJointeRepository.findById(pieceJointeId)
                .orElseThrow(() -> new ResourceNotFoundException("Piece jointe introuvable (id=" + pieceJointeId + ")"));
    }

    // Liste les demandes encore EN_ATTENTE depuis plus de {jours} jours.
    // Utilise par le workflow n8n de rappel automatique (n4).
    
    public List<com.quoteflow.dto.demande.RappelResponse> demandesEnRetard(int jours) {
        java.time.LocalDateTime seuil = java.time.LocalDateTime.now().minusDays(jours);
        return demandeRepository
                .findByStatutAndDateCreationBefore(StatutDemande.EN_ATTENTE, seuil)
                .stream()
                .map(d -> com.quoteflow.dto.demande.RappelResponse.builder()
                        .id(d.getId())
                        .reference(d.getReference())
                        .description(d.getDescription())
                        .clientNomComplet(d.getClient().getPrenom() + " " + d.getClient().getNom())
                        .clientEmail(d.getClient().getEmail())
                        .adminEmail(adminEmail)
                        .joursEnAttente(java.time.Duration.between(
                                d.getDateCreation(), java.time.LocalDateTime.now()).toDays())
                        .build())
                .toList();
    }

    public DemandeDevis getEntity(Long id) {
        return demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable (id=" + id + ")"));
    }

    private String genererReference(Long id) {
        return String.format("QF-%d-%04d", Year.now().getValue(), id);
    }

    private DemandeResponse toResponse(DemandeDevis d) {
        return DemandeResponse.builder()
                .id(d.getId())
                .reference(d.getReference())
                .description(d.getDescription())
                .budget(d.getBudget())
                .statut(d.getStatut().name())
                .dateCreation(d.getDateCreation())
                .dateMaj(d.getDateMaj())
                .clientNomComplet(d.getClient().getPrenom() + " " + d.getClient().getNom())
                .employeId(d.getEmploye() != null ? d.getEmploye().getId() : null)
                .employeNomComplet(d.getEmploye() != null
                        ? d.getEmploye().getPrenom() + " " + d.getEmploye().getNom() : null)
                .devisId(devisRepository.findByDemandeId(d.getId()).map(dv -> dv.getId()).orElse(null))
                .piecesJointes(d.getPiecesJointes().stream()
                        .map(p -> DemandeResponse.PieceJointeDto.builder()
                                .id(p.getId()).nomOriginal(p.getNomOriginal())
                                .tailleOctets(p.getTailleOctets()).build())
                        .toList())
                .build();
    }
}
