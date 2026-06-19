package com.quoteflow.connect.entity;

import com.quoteflow.entity.DemandeDevis; // <-- adapter
import com.quoteflow.entity.User;          // <-- adapter
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Ã‰vÃ©nement de la timeline d'une demande. EntitÃ© dÃ©diÃ©e (et non un Message SYSTEME)
 * car un Ã©vÃ©nement mÃ©tier n'est pas une discussion : il n'a pas d'accusÃ© de
 * lecture, pas de piÃ¨ce jointe, et se requÃªte indÃ©pendamment.
 */
@Entity
@Table(name = "activite", indexes =
        @Index(name = "idx_activite_demande", columnList = "demande_id"))
@Getter
@Setter
public class Activite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false)
    private DemandeDevis demande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ActiviteType type;

    /** Acteur Ã  l'origine de l'Ã©vÃ©nement ; null si dÃ©clenchÃ© par le systÃ¨me. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acteur_id")
    private User acteur;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    @PrePersist
    void prePersist() {
        if (dateCreation == null) dateCreation = Instant.now();
    }
}

