package com.quoteflow.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "pieces_jointes")
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomFichier;
    private String type;       // content-type, ex: application/pdf
    private Long taille;       // en octets
    private String chemin;     // chemin de stockage sur le serveur

    @ManyToOne
    @JoinColumn(name = "demande_id")
    @JsonIgnore
    private DemandeDevis demande;

    public PieceJointe() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getTaille() { return taille; }
    public void setTaille(Long taille) { this.taille = taille; }

    public String getChemin() { return chemin; }
    public void setChemin(String chemin) { this.chemin = chemin; }

    public DemandeDevis getDemande() { return demande; }
    public void setDemande(DemandeDevis demande) { this.demande = demande; }
}
