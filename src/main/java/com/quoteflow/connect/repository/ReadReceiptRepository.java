package com.quoteflow.connect.repository;

import com.quoteflow.connect.entity.ReadReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadReceiptRepository extends JpaRepository<ReadReceipt, Long> {
    boolean existsByMessageIdAndUtilisateurId(Long messageId, Long utilisateurId);
}

