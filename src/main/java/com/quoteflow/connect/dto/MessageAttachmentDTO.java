package com.quoteflow.connect.dto;

/** PiÃ¨ce jointe exposÃ©e au front. 'url' = endpoint de tÃ©lÃ©chargement sÃ©curisÃ©. */
public record MessageAttachmentDTO(
        Long id,
        String nomFichier,
        String url,
        String typeMime,
        Long taille
) {}

