package com.quoteflow.connect.entity;

import com.quoteflow.entity.DemandeDevis; // <-- adapter au package rÃ©el de votre entitÃ©
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Espace collaboratif rattachÃ© Ã  UNE demande de devis.
 * Relation 1-1 avec DemandeDevis : chaque demande possÃ¨de son propre espace
 * (la discussion est attachÃ©e Ã  la demande, pas Ã  un couple d'utilisateurs).
 */
@Entity
@Table(name = "conversation",
        uniqueConstraints = @UniqueConstraint(columnNames = "demande_id"))
@Getter
@Setter
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false, unique = true)
    private DemandeDevis demande;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    private Instant dateDernierMessage;

    @PrePersist
    void prePersist() {
        if (dateCreation == null) dateCreation = Instant.now();
    }
}

