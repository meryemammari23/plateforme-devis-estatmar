package com.quoteflow.service;

import com.quoteflow.dto.devis.DevisRequest;
import com.quoteflow.dto.devis.DevisResponse;
import com.quoteflow.dto.devis.LigneDevisRequest;
import com.quoteflow.entity.Devis;
import com.quoteflow.entity.DemandeDevis;
import com.quoteflow.entity.LigneDevis;
import com.quoteflow.entity.User;
import com.quoteflow.entity.enums.StatutDemande;
import com.quoteflow.exception.BusinessException;
import com.quoteflow.exception.ResourceNotFoundException;
import com.quoteflow.repository.DemandeDevisRepository;
import com.quoteflow.repository.DevisRepository;
import com.quoteflow.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

 // redaction du devis par l'employe.
 //A la creation : calcul du total, generation PDF (iText), passage de la demande
 //au statut VALIDE et envoi du PDF au client via n8n.

@Service
public class DevisService {

    private final DevisRepository devisRepository;
    private final DemandeDevisRepository demandeRepository;
    private final UserRepository userRepository;
    private final PdfService pdfService;
    private final WebhookService webhookService;
    private final NotificationService notificationService;
    private final String apiBaseUrl;
    private final String frontUrl;

    public DevisService(DevisRepository devisRepository, DemandeDevisRepository demandeRepository,
                        UserRepository userRepository, PdfService pdfService,
                        WebhookService webhookService, NotificationService notificationService,
                        @org.springframework.beans.factory.annotation.Value("${app.api.base-url}") String apiBaseUrl,
                        @org.springframework.beans.factory.annotation.Value("${app.front.url:http://localhost:5173}") String frontUrl) {
        this.devisRepository = devisRepository;
        this.demandeRepository = demandeRepository;
        this.userRepository = userRepository;
        this.pdfService = pdfService;
        this.webhookService = webhookService;
        this.notificationService = notificationService;
        this.apiBaseUrl = apiBaseUrl;
        this.frontUrl = frontUrl;
    }

    // Cree et valide le devis : total -> PDF -> statut VALIDE -> webhook envoi. 
    @Transactional
    public DevisResponse creerEtValider(Long employeId, DevisRequest request) {
        DemandeDevis demande = demandeRepository.findById(request.getDemandeId())
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable"));

        if (devisRepository.findByDemandeId(demande.getId()).isPresent()) {
            throw new BusinessException("Un devis existe deja pour cette demande");
        }
        if (demande.getStatut() == StatutDemande.REFUSE) {
            throw new BusinessException("Impossible de rediger un devis pour une demande refusee");
        }

        User employe = userRepository.findById(employeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employe introuvable"));

        // Securite metier : l'employe ne traite que ses dossiers attribues.

        if (demande.getEmploye() == null || !demande.getEmploye().getId().equals(employeId)) {
            throw new BusinessException("Cette demande ne vous est pas attribuee");
        }

        Devis devis = Devis.builder()
                .demande(demande)
                .employe(employe)
                .commentaire(request.getCommentaire())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (LigneDevisRequest l : request.getLignes()) {
            BigDecimal sousTotal = l.getPrixUnitaire().multiply(BigDecimal.valueOf(l.getQuantite()));
            LigneDevis ligne = LigneDevis.builder()
                    .designation(l.getDesignation())
                    .quantite(l.getQuantite())
                    .prixUnitaire(l.getPrixUnitaire())
                    .sousTotal(sousTotal)
                    .build();
            devis.addLigne(ligne);
            total = total.add(sousTotal);
        }
        devis.setMontantTotal(total);

        // Persistance d'abord (la reference de la demande doit exister pour le PDF).
        devis = devisRepository.save(devis);

        // Generation du PDF via iText 7.
        String cheminPdf = pdfService.genererPdfDevis(devis);
        devis.setFichierPDF(cheminPdf);
        devis = devisRepository.save(devis);

        // Transition automatique : la demande passe a VALIDE.
        demande.setStatut(StatutDemande.VALIDE);
        demandeRepository.save(demande);

        // Notification d'envoi du devis au client via n8n.
        // Lien pour le client : page "Mes demandes" du frontend (connexion +
        // bouton de telechargement securise), au lieu de l'API protegee directe.
        String pdfUrl = frontUrl + "/client/mes-demandes";
        webhookService.notifierEnvoiDevis(demande.getClient().getEmail(),
                demande.getClient().getNom(), demande.getReference(), pdfUrl);
        notificationService.tracer("ENVOI_DEVIS",
                "Devis envoye pour " + demande.getReference(), demande.getClient(), demande, true);

        return toResponse(devis);
    }

    public DevisResponse voir(Long id) {
        return toResponse(getEntity(id));
    }

    public Devis getEntity(Long id) {
        return devisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devis introuvable (id=" + id + ")"));
    }

    // Chemin du PDF pour telechargement. 
    public String cheminPdf(Long devisId) {
        Devis devis = getEntity(devisId);
        if (devis.getFichierPDF() == null) {
            throw new BusinessException("Aucun PDF disponible pour ce devis");
        }
        return devis.getFichierPDF();
    }

    private DevisResponse toResponse(Devis d) {
        List<DevisResponse.LigneDto> lignes = d.getLignes().stream()
                .map(l -> DevisResponse.LigneDto.builder()
                        .designation(l.getDesignation())
                        .quantite(l.getQuantite())
                        .prixUnitaire(l.getPrixUnitaire())
                        .sousTotal(l.getSousTotal())
                        .build())
                .toList();
        return DevisResponse.builder()
                .id(d.getId())
                .demandeId(d.getDemande().getId())
                .demandeReference(d.getDemande().getReference())
                .montantTotal(d.getMontantTotal())
                .commentaire(d.getCommentaire())
                .pdfDisponible(d.getFichierPDF() != null)
                .dateCreation(d.getDateCreation())
                .employeNomComplet(d.getEmploye().getPrenom() + " " + d.getEmploye().getNom())
                .lignes(lignes)
                .build();
    }
}
