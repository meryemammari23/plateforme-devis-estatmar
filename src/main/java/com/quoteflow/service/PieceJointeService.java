package com.quoteflow.service;

import com.quoteflow.entity.DemandeDevis;
import com.quoteflow.entity.PieceJointe;
import com.quoteflow.exception.ResourceNotFoundException;
import com.quoteflow.repository.DemandeDevisRepository;
import com.quoteflow.repository.PieceJointeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Upload de pieces jointes (le "trou fonctionnel" du module client).
 * Stocke le fichier sur disque et l'enregistre en base, rattache a la demande.
 */
@Service
public class PieceJointeService {

    private final PieceJointeRepository pjRepo;
    private final DemandeDevisRepository demandeRepo;

    @Value("${app.uploads-dir:./uploads}")
    private String uploadsDir;

    public PieceJointeService(PieceJointeRepository pjRepo,
                              DemandeDevisRepository demandeRepo) {
        this.pjRepo = pjRepo;
        this.demandeRepo = demandeRepo;
    }

    public PieceJointe upload(Long demandeId, MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide ou absent");
        }
        DemandeDevis demande = demandeRepo.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + demandeId));

        try {
            Path dir = Paths.get(uploadsDir);
            Files.createDirectories(dir);

            String original = fichier.getOriginalFilename() != null
                    ? fichier.getOriginalFilename() : "fichier";
            String stocke = UUID.randomUUID() + "_" + original.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path cible = dir.resolve(stocke);
            Files.write(cible, fichier.getBytes());

            PieceJointe pj = new PieceJointe();
            pj.setNomFichier(original);
            pj.setType(fichier.getContentType());
            pj.setTaille(fichier.getSize());
            pj.setChemin(cible.toAbsolutePath().toString());
            pj.setDemande(demande);

            return pjRepo.save(pj);
        } catch (Exception e) {
            throw new RuntimeException("Echec de l'upload de la piece jointe", e);
        }
    }
}
