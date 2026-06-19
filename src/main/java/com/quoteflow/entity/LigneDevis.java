package com.quoteflow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lignes_devis")
public class LigneDevis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String designation;
    private int quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;   // calculé : quantite * prixUnitaire

    @ManyToOne
    @JoinColumn(name = "devis_id")
    @JsonIgnore
    private Devis devis;

    public LigneDevis() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public BigDecimal getSousTotal() { return sousTotal; }
    public void setSousTotal(BigDecimal sousTotal) { this.sousTotal = sousTotal; }

    public Devis getDevis() { return devis; }
    public void setDevis(Devis devis) { this.devis = devis; }
}
