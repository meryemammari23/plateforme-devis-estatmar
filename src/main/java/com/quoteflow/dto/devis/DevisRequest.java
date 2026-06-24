package com.quoteflow.dto.devis;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

// Creation d'un devis : rattache a une demande, avec ses lignes

@Data
public class DevisRequest {

    @NotNull(message = "L'identifiant de la demande est obligatoire")
    private Long demandeId;

    private String commentaire;

    @NotEmpty(message = "Le devis doit contenir au moins une ligne")
    @Valid
    private List<LigneDevisRequest> lignes;
}
