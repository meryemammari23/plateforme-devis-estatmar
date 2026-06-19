package com.quoteflow.repository;

import com.quoteflow.entity.Devis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevisRepository extends JpaRepository<Devis, Long> {
}
