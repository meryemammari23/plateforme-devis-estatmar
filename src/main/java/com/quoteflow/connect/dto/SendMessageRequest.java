package com.quoteflow.connect.dto;

import jakarta.validation.constraints.Size;

/** Corps d'un message texte (REST ou STOMP). Les fichiers passent en multipart. */
public record SendMessageRequest(
        @Size(max = 5000, message = "Message trop long (5000 caractÃ¨res max)")
        String contenu
) {}

