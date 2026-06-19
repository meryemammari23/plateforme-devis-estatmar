package com.quoteflow.connect.dto;

import java.time.Instant;
import java.util.List;

public record MessageDTO(
        Long id,
        Long conversationId,
        Long expediteurId,
        String expediteurNom,
        String expediteurRole,
        String contenu,
        String type,
        boolean lu,
        Instant dateCreation,
        List<MessageAttachmentDTO> attachments
) {}

