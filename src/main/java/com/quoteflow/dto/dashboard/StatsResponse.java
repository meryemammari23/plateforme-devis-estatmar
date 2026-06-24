package com.quoteflow.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

// Statistiques globales pour le tableau de bord admin. 

@Data
@Builder
public class StatsResponse {
    private long totalDemandes;
    private long demandesEnAttente;
    private long demandesEnCours;
    private long demandesValidees;
    private long demandesRefusees;
    private long totalEmployes;
    private long totalClients;
    /** Repartition utilisable directement par Recharts (statut -> nombre). */
    private Map<String, Long> repartitionParStatut;
}
