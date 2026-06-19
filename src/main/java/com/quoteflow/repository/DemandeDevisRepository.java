package com.quoteflow.repository;

import com.quoteflow.entity.DemandeDevis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DemandeDevisRepository extends JpaRepository<DemandeDevis, Long> {
    List<DemandeDevis> findByClientId(Long clientId);
    List<DemandeDevis> findByEmployeId(Long employeId);
}
