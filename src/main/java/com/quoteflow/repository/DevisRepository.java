package com.quoteflow.repository;

import com.quoteflow.entity.Devis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DevisRepository extends JpaRepository<Devis, Long> {

    Optional<Devis> findByDemandeId(Long demandeId);
}
