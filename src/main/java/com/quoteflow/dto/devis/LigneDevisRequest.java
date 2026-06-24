package com.quoteflow.dto.devis;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

// Une ligne du devis saisie par l'employe

@Data
public class LigneDevisRequest {

    @NotBlank(message = "La designation est obligatoire")
    private String designation;

    @NotNull(message = "La quantite est obligatoire")
    @Min(value = 1, message = "La quantite doit etre au moins 1")
    private Integer quantite;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix unitaire doit etre positif")
    private BigDecimal prixUnitaire;
}
