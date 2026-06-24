package com.quoteflow.repository;

import com.quoteflow.entity.DemandeDevis;
import com.quoteflow.entity.enums.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DemandeDevisRepository extends JpaRepository<DemandeDevis, Long> {

    List<DemandeDevis> findByClientId(Long clientId);

    List<DemandeDevis> findByEmployeId(Long employeId);

    long countByStatut(StatutDemande statut);

    long countByEmployeIdAndStatut(Long employeId, StatutDemande statut);

    long countByEmployeId(Long employeId);

    java.util.List<com.quoteflow.entity.DemandeDevis> findByStatutAndDateCreationBefore(
            com.quoteflow.entity.enums.StatutDemande statut, java.time.LocalDateTime seuil);
}
