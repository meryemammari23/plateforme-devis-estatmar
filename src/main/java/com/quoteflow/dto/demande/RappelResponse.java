package com.quoteflow.dto.demande;

import lombok.Builder;
import lombok.Data;

//Donnees d'une demande en retard, destinees au workflow n8n de rappel

@Data
@Builder
public class RappelResponse {
    private Long id;
    private String reference;
    private String description;
    private String clientNomComplet;
    private String clientEmail;
    private String adminEmail;
    private long joursEnAttente;
}
