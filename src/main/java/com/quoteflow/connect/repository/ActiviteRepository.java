package com.quoteflow.connect.repository;

import com.quoteflow.connect.entity.Activite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActiviteRepository extends JpaRepository<Activite, Long> {
    List<Activite> findByDemandeIdOrderByDateCreationAsc(Long demandeId);
}

