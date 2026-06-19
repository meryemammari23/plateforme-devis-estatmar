package com.quoteflow.connect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * PiÃ¨ce jointe d'un MESSAGE. Distincte de PieceJointe (rattachÃ©e Ã  la demande),
 * mais les deux partagent le mÃªme FileStorageService pour le stockage physique.
 */
@Entity
@Table(name = "message_attachment", indexes =
        @Index(name = "idx_attachment_message", columnList = "message_id"))
@Getter
@Setter
public class MessageAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @Column(nullable = false)
    private String nomFichier;

    /** Chemin relatif sur le disque serveur (ne JAMAIS exposer au client). */
    @Column(nullable = false)
    private String chemin;

    @Column(length = 150)
    private String typeMime;

    private Long taille;
}

