package com.quoteflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

// Ligne d'un devis : designation, quantite, prix unitaire.
// Le sous-total est calcule automatiquement (quantite * prixUnitaire).

@Entity
@Table(name = "lignes_devis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneDevis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String designation;

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "sous_total", precision = 10, scale = 2)
    private BigDecimal sousTotal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "devis_id", nullable = false)
    private Devis devis;

    // Recalcule le sous-total avant chaque persist/update. 
    
    @PrePersist
    @PreUpdate
    protected void calculerSousTotal() {
        if (quantite != null && prixUnitaire != null) {
            this.sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }
}
