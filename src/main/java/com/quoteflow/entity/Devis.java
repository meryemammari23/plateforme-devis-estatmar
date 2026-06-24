package com.quoteflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Devis redige par l'employe pour une demande donnee.
//NOTE: le CDC mentionnait un champ 'adminId' ; renomme en 'employe'
// car c'est l'employe (role EMPLOYE) qui redige et soumet le devis.

@Entity
@Table(name = "devis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Devis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "montant_total", precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    /** Chemin du PDF genere par iText (rempli a la validation). */
    @Column(name = "fichier_pdf", length = 500)
    private String fichierPDF;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    /** Demande couverte par ce devis (relation 1-1 logique). */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "demande_id", nullable = false, unique = true)
    private DemandeDevis demande;

    /** Employe auteur du devis. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private User employe;

    @OneToMany(mappedBy = "devis", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneDevis> lignes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }

    /** Ajoute une ligne en maintenant la coherence bidirectionnelle. */
    public void addLigne(LigneDevis ligne) {
        ligne.setDevis(this);
        this.lignes.add(ligne);
    }
}
