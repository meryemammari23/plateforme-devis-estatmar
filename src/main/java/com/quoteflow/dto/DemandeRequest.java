package com.quoteflow.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class DemandeRequest {

    @NotBlank(message = "La description est obligatoire")
    private String description;

    private BigDecimal budget;

    /**
     * Id du client. Optionnel ici : dans le projet intégré, le client est
     * déduit du token JWT. Pour le test local, on peut le passer explicitement ;
     * s'il est null, le service utilise le client de démonstration.
     */
    private Long clientId;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
}
