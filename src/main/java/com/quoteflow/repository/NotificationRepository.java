package com.quoteflow.repository;

import com.quoteflow.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDemandeId(Long demandeId);
}
