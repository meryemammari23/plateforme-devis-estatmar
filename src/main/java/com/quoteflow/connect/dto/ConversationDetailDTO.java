package com.quoteflow.connect.dto;

import java.time.Instant;
import java.util.List;

/** Vue complÃ¨te d'un espace : en-tÃªte demande + messages + timeline. */
public record ConversationDetailDTO(
        Long conversationId,
        Long demandeId,
        String referenceDemande,
        String descriptionDemande,
        String statutDemande,
        Instant dateCreation,
        List<MessageDTO> messages,
        List<ActiviteDTO> activites
) {}

