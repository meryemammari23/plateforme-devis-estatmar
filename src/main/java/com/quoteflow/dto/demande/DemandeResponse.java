package com.quoteflow.dto.demande;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Vue d'une demande renvoyee au front. 
@Data
@Builder
public class DemandeResponse {
    private Long id;
    private String reference;
    private String description;
    private BigDecimal budget;
    private String statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMaj;
    private String clientNomComplet;
    private Long employeId;
    private String employeNomComplet;
    // Id du devis si la demande a ete validee (sinon null) : permet le telechargement cote client. 
    private Long devisId;
    private List<PieceJointeDto> piecesJointes;

    @Data
    @Builder
    public static class PieceJointeDto {
        private Long id;
        private String nomOriginal;
        private Long tailleOctets;
    }
}
