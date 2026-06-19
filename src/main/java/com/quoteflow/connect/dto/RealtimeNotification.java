package com.quoteflow.connect.dto;

import java.time.Instant;

/** Payload poussÃ© via WebSocket sur /user/queue/notifications. */
public record RealtimeNotification(
        String type,          // NOUVEAU_MESSAGE, NOUVEAU_DOCUMENT, STATUT_CHANGE, DEVIS_GENERE
        Long conversationId,
        Long demandeId,
        String titre,
        String apercu,
        Instant date
) {
    public static RealtimeNotification of(String type, Long conversationId, Long demandeId,
                                          String titre, String apercu) {
        return new RealtimeNotification(type, conversationId, demandeId, titre, apercu, Instant.now());
    }
}

