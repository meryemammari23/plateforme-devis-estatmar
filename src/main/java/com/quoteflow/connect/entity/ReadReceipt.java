package com.quoteflow.connect.entity;

import com.quoteflow.entity.User; // <-- adapter au package rÃ©el
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * AccusÃ© de lecture d'un message par un utilisateur (statut Â« Vu Â»).
 * UnicitÃ© (message, utilisateur) : un seul accusÃ© par couple.
 */
@Entity
@Table(name = "read_receipt",
        uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "utilisateur_id"}))
@Getter
@Setter
public class ReadReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    @Column(nullable = false)
    private Instant dateLecture;

    @PrePersist
    void prePersist() {
        if (dateLecture == null) dateLecture = Instant.now();
    }
}

