package com.quoteflow.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "demandes")
public class DemandeDevis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String reference;

    @Column(length = 2000)
    private String description;

    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    private LocalDateTime dateCreation = LocalDateTime.now();
    private LocalDateTime dateMaj = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "client_id")
    private User client;

    @ManyToOne
    @JoinColumn(name = "employe_id")
    private User employe;

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PieceJointe> piecesJointes = new ArrayList<>();

    public DemandeDevis() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }

    public StatutDemande getStatut() { return statut; }
    public void setStatut(StatutDemande statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDateTime dateMaj) { this.dateMaj = dateMaj; }

    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }

    public User getEmploye() { return employe; }
    public void setEmploye(User employe) { this.employe = employe; }

    public List<PieceJointe> getPiecesJointes() { return piecesJointes; }
    public void setPiecesJointes(List<PieceJointe> piecesJointes) { this.piecesJointes = piecesJointes; }
}
