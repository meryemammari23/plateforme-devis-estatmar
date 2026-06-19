package com.quoteflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "devis")
public class Devis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal montantTotal;

    @Column(length = 2000)
    private String commentaire;

    private String fichierPDF;   // chemin du PDF généré sur le serveur

    private LocalDateTime dateCreation = LocalDateTime.now();

    @OneToOne
    @JoinColumn(name = "demande_id")
    private DemandeDevis demande;

    @ManyToOne
    @JoinColumn(name = "employe_id")
    private User employe;   // l'employé qui rédige le devis (anciennement adminId)

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneDevis> lignes = new ArrayList<>();

    public Devis() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal montantTotal) { this.montantTotal = montantTotal; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public String getFichierPDF() { return fichierPDF; }
    public void setFichierPDF(String fichierPDF) { this.fichierPDF = fichierPDF; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public DemandeDevis getDemande() { return demande; }
    public void setDemande(DemandeDevis demande) { this.demande = demande; }

    public User getEmploye() { return employe; }
    public void setEmploye(User employe) { this.employe = employe; }

    public List<LigneDevis> getLignes() { return lignes; }
    public void setLignes(List<LigneDevis> lignes) { this.lignes = lignes; }
}
