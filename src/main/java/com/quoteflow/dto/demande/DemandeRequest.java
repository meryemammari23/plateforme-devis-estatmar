package com.quoteflow.dto.demande;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

// Saisie d'une nouvelle demande de devis par le client

@Data
public class DemandeRequest {

    @NotBlank(message = "La description du projet est obligatoire")
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le budget doit etre positif")
    private BigDecimal budget;
}
