package com.quoteflow.service;

import com.quoteflow.dto.DevisRequest;
import com.quoteflow.dto.LigneDevisRequest;
import com.quoteflow.entity.*;
import com.quoteflow.exception.ResourceNotFoundException;
import com.quoteflow.repository.DemandeDevisRepository;
import com.quoteflow.repository.DevisRepository;
import com.quoteflow.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DevisService {

    private final DevisRepository devisRepo;
    private final DemandeDevisRepository demandeRepo;
    private final UserRepository userRepo;
    private final PdfService pdfService;
    private final N8nWebhookService n8n;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    public DevisService(DevisRepository devisRepo,
                        DemandeDevisRepository demandeRepo,
                        UserRepository userRepo,
                        PdfService pdfService,
                        N8nWebhookService n8n) {
        this.devisRepo = devisRepo;
        this.demandeRepo = demandeRepo;
        this.userRepo = userRepo;
        this.pdfService = pdfService;
        this.n8n = n8n;
    }

    /**
     * Cree un devis a partir d'une demande, calcule les totaux, genere le PDF,
     * passe la demande a VALIDE et declenche le webhook d'envoi.
     */
    public Devis creer(DevisRequest req) {
        DemandeDevis demande = demandeRepo.findById(req.getDemandeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande introuvable : " + req.getDemandeId()));

        User employe = resoudreEmploye(req.getEmployeId());

        Devis devis = new Devis();
        devis.setDemande(demande);
        devis.setEmploye(employe);
        devis.setCommentaire(req.getCommentaire());

        BigDecimal total = BigDecimal.ZERO;
        for (LigneDevisRequest lr : req.getLignes()) {
            LigneDevis ligne = new LigneDevis();
            ligne.setDesignation(lr.getDesignation());
            ligne.setQuantite(lr.getQuantite());
            ligne.setPrixUnitaire(lr.getPrixUnitaire());
            BigDecimal sousTotal = lr.getPrixUnitaire()
                    .multiply(BigDecimal.valueOf(lr.getQuantite()));
            ligne.setSousTotal(sousTotal);
            ligne.setDevis(devis);
            devis.getLignes().add(ligne);
            total = total.add(sousTotal);
        }
        devis.setMontantTotal(total);

        // 1. sauvegarde (genere l'id necessaire au chemin/URL)
        Devis saved = devisRepo.save(devis);

        // 2. generation du PDF + stockage du chemin
        String chemin = pdfService.genererEtSauvegarder(saved);
        saved.setFichierPDF(chemin);
        saved = devisRepo.save(saved);

        // 3. passage de la demande a VALIDE
        demande.setStatut(StatutDemande.VALIDE);
        demandeRepo.save(demande);

        // 4. webhooks n8n -> client
        if (demande.getClient() != null) {
            String pdfUrl = publicBaseUrl + "/api/devis/" + saved.getId() + "/pdf";
            n8n.envoiDevis(demande.getClient().getEmail(), demande.getClient().getNom(),
                    demande.getReference(), pdfUrl);
            n8n.changementStatut(demande.getClient().getEmail(), demande.getClient().getNom(),
                    demande.getReference(), StatutDemande.VALIDE.toString());
        }

        return saved;
    }

    public Devis findById(Long id) {
        return devisRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devis introuvable : " + id));
    }

    /** Renvoie les octets du PDF : lit le fichier stocke, sinon regenere. */
    public byte[] getPdfBytes(Long id) {
        Devis devis = findById(id);
        try {
            if (devis.getFichierPDF() != null) {
                Path p = Paths.get(devis.getFichierPDF());
                if (Files.exists(p)) {
                    return Files.readAllBytes(p);
                }
            }
        } catch (Exception ignored) {
            // on retombe sur la regeneration
        }
        return pdfService.genererDevis(devis);
    }

    private User resoudreEmploye(Long employeId) {
        if (employeId != null) {
            return userRepo.findById(employeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employe introuvable : " + employeId));
        }
        return userRepo.findByRole(Role.EMPLOYE).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Aucun employe de demonstration"));
    }
}
