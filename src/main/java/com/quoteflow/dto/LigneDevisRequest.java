package com.quoteflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class LigneDevisRequest {

    @NotBlank(message = "La désignation est obligatoire")
    private String designation;

    @Positive(message = "La quantité doit être positive")
    private int quantite;

    @NotNull(message = "Le prix unitaire est obligatoire")
    private BigDecimal prixUnitaire;

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    public BigDecimal getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(BigDecimal prixUnitaire) { this.prixUnitaire = prixUnitaire; }
}
