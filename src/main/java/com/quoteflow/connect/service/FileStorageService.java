package com.quoteflow.connect.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

/**
 * Service de stockage de fichiers sur disque local â€” PARTAGÃ‰ par tout le projet
 * (messages ET PieceJointe des demandes). Stocke sous un nom alÃ©atoire pour
 * Ã©viter les collisions et les attaques par traversÃ©e de chemin.
 *
 * PropriÃ©tÃ© application.yml :
 *   quoteflow.storage.root: ./data/uploads
 */
@Service
public class FileStorageService {

    private final Path root;

    private static final Set<String> TYPES_AUTORISES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "image/png",
            "image/jpeg"
    );
    private static final long TAILLE_MAX = 15 * 1024 * 1024; // 15 Mo

    public FileStorageService(@Value("${quoteflow.storage.root:./data/uploads}") String root) {
        this.root = Paths.get(root).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de crÃ©er le dossier de stockage : " + root, e);
        }
    }

    /**
     * Stocke un fichier et retourne son chemin RELATIF (Ã  persister en base).
     * @param sousDossier ex: "messages" ou "demandes"
     */
    public StoredFile store(MultipartFile file, String sousDossier) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("Fichier vide");
        if (file.getSize() > TAILLE_MAX)
            throw new IllegalArgumentException("Fichier trop volumineux (15 Mo max)");
        String mime = file.getContentType();
        if (mime == null || !TYPES_AUTORISES.contains(mime))
            throw new IllegalArgumentException("Type non autorisÃ© : " + mime + " (PDF, DOCX, PNG, JPG)");

        try {
            Path dir = root.resolve(sousDossier).normalize();
            if (!dir.startsWith(root)) throw new SecurityException("Chemin invalide");
            Files.createDirectories(dir);

            String ext = extension(file.getOriginalFilename());
            String nomStocke = UUID.randomUUID() + ext;
            Path cible = dir.resolve(nomStocke);

            try (var in = file.getInputStream()) {
                Files.copy(in, cible, StandardCopyOption.REPLACE_EXISTING);
            }
            String relatif = root.relativize(cible).toString().replace('\\', '/');
            return new StoredFile(relatif, sanitize(file.getOriginalFilename()), mime, file.getSize());
        } catch (IOException e) {
            throw new RuntimeException("Ã‰chec de l'enregistrement du fichier", e);
        }
    }

    public Resource load(String cheminRelatif) {
        try {
            Path fichier = root.resolve(cheminRelatif).normalize();
            if (!fichier.startsWith(root)) throw new SecurityException("AccÃ¨s refusÃ©");
            Resource r = new UrlResource(fichier.toUri());
            if (!r.exists() || !r.isReadable())
                throw new RuntimeException("Fichier introuvable : " + cheminRelatif);
            return r;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Chemin de fichier invalide", e);
        }
    }

    private String extension(String nom) {
        if (nom == null) return "";
        int i = nom.lastIndexOf('.');
        return i >= 0 ? nom.substring(i).toLowerCase() : "";
    }

    private String sanitize(String nom) {
        if (nom == null) return "fichier";
        return Paths.get(nom).getFileName().toString();
    }

    /** MÃ©tadonnÃ©es du fichier stockÃ©. */
    public record StoredFile(String chemin, String nomOriginal, String typeMime, long taille) {}
}

