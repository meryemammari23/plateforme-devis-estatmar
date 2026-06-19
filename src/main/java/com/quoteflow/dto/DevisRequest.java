package com.quoteflow.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class DevisRequest {

    @NotNull(message = "L'id de la demande est obligatoire")
    private Long demandeId;

    private String commentaire;

    @NotEmpty(message = "Au moins une ligne de devis est requise")
    private List<LigneDevisRequest> lignes;

    /**
     * Id de l'employé rédacteur. Optionnel pour le test local
     * (l'employé de démo est utilisé par défaut).
     */
    private Long employeId;

    public Long getDemandeId() { return demandeId; }
    public void setDemandeId(Long demandeId) { this.demandeId = demandeId; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public List<LigneDevisRequest> getLignes() { return lignes; }
    public void setLignes(List<LigneDevisRequest> lignes) { this.lignes = lignes; }

    public Long getEmployeId() { return employeId; }
    public void setEmployeId(Long employeId) { this.employeId = employeId; }
}
