package com.quoteflow.service;

import com.quoteflow.dto.dashboard.StatsResponse;
import com.quoteflow.entity.enums.Role;
import com.quoteflow.entity.enums.StatutDemande;
import com.quoteflow.exception.ResourceNotFoundException;
import com.quoteflow.repository.DemandeDevisRepository;
import com.quoteflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

//  indicateurs pour le tableau de bord admin (Recharts). 

@Service
public class DashboardService {

    private final DemandeDevisRepository demandeRepository;
    private final UserRepository userRepository;

    public DashboardService(DemandeDevisRepository demandeRepository, UserRepository userRepository) {
        this.demandeRepository = demandeRepository;
        this.userRepository = userRepository;
    }

    public StatsResponse statsGlobales() {
        long enAttente = demandeRepository.countByStatut(StatutDemande.EN_ATTENTE);
        long enCours = demandeRepository.countByStatut(StatutDemande.EN_COURS);
        long valide = demandeRepository.countByStatut(StatutDemande.VALIDE);
        long refuse = demandeRepository.countByStatut(StatutDemande.REFUSE);

        Map<String, Long> repartition = new LinkedHashMap<>();
        repartition.put("EN_ATTENTE", enAttente);
        repartition.put("EN_COURS", enCours);
        repartition.put("VALIDE", valide);
        repartition.put("REFUSE", refuse);

        return StatsResponse.builder()
                .totalDemandes(demandeRepository.count())
                .demandesEnAttente(enAttente)
                .demandesEnCours(enCours)
                .demandesValidees(valide)
                .demandesRefusees(refuse)
                .totalEmployes(userRepository.findByRole(Role.EMPLOYE).size())
                .totalClients(userRepository.findByRole(Role.CLIENT).size())
                .repartitionParStatut(repartition)
                .build();
    }

    // Performance d'un employe : volume traite et devis valides. 
    
    public Map<String, Long> statsEmploye(Long employeId) {
        if (userRepository.findById(employeId).isEmpty()) {
            throw new ResourceNotFoundException("Employe introuvable (id=" + employeId + ")");
        }
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("totalDossiers", demandeRepository.countByEmployeId(employeId));
        stats.put("enCours", demandeRepository.countByEmployeIdAndStatut(employeId, StatutDemande.EN_COURS));
        stats.put("valides", demandeRepository.countByEmployeIdAndStatut(employeId, StatutDemande.VALIDE));
        stats.put("refuses", demandeRepository.countByEmployeIdAndStatut(employeId, StatutDemande.REFUSE));
        return stats;
    }
}
