package com.quoteflow.connect.mapper;

import com.quoteflow.connect.dto.*;
import com.quoteflow.connect.entity.*;
import com.quoteflow.entity.User; // <-- adapter

import java.util.List;

/**
 * Conversion entitÃ©s -> DTO. CentralisÃ© pour Ã©viter d'exposer les entitÃ©s JPA
 * (et leurs proxys lazy) directement dans les rÃ©ponses HTTP/WebSocket.
 * Adaptez les getters de User (getNom/getPrenom/getRole) Ã  votre entitÃ©.
 */
public final class ConnectMapper {

    private ConnectMapper() {}

    public static String nomComplet(User u) {
        if (u == null) return "Inconnu";
        return (safe(u.getPrenom()) + " " + safe(u.getNom())).trim();
    }

    private static String safe(String s) { return s == null ? "" : s; }

    public static MessageAttachmentDTO toAttachmentDTO(MessageAttachment a) {
        return new MessageAttachmentDTO(
                a.getId(),
                a.getNomFichier(),
                "/api/connect/files/" + a.getId() + "/download",
                a.getTypeMime(),
                a.getTaille()
        );
    }

    public static MessageDTO toMessageDTO(Message m) {
        User exp = m.getExpediteur();
        List<MessageAttachmentDTO> atts = m.getAttachments().stream()
                .map(ConnectMapper::toAttachmentDTO)
                .toList();
        return new MessageDTO(
                m.getId(),
                m.getConversation().getId(),
                exp != null ? exp.getId() : null,
                nomComplet(exp),
                exp != null && exp.getRole() != null ? exp.getRole().toString() : null,
                m.getContenu(),
                m.getType().name(),
                m.isLu(),
                m.getDateCreation(),
                atts
        );
    }

    public static ActiviteDTO toActiviteDTO(Activite a) {
        User acteur = a.getActeur();
        return new ActiviteDTO(
                a.getId(),
                a.getType().name(),
                a.getDescription(),
                acteur != null ? acteur.getId() : null,
                acteur != null ? nomComplet(acteur) : "SystÃ¨me",
                a.getDateCreation()
        );
    }
}

