package com.quoteflow.dto.devis;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Vue d'un devis renvoyee au front

@Builder
public class DevisResponse {
    private Long id;
    private Long demandeId;
    private String demandeReference;
    private BigDecimal montantTotal;
    private String commentaire;
    private boolean pdfDisponible;
    private LocalDateTime dateCreation;
    private String employeNomComplet;
    private List<LigneDto> lignes;

    @Data
    @Builder
    public static class LigneDto {
        private String designation;
        private Integer quantite;
        private BigDecimal prixUnitaire;
        private BigDecimal sousTotal;
    }
}
