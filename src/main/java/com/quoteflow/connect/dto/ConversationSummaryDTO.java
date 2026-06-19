package com.quoteflow.connect.dto;

import java.time.Instant;

/** Ligne de la colonne gauche (liste des demandes / conversations). */
public record ConversationSummaryDTO(
        Long conversationId,
        Long demandeId,
        String referenceDemande,
        String statutDemande,
        String dernierMessage,
        Instant dateDernierMessage,
        long messagesNonLus
) {}

