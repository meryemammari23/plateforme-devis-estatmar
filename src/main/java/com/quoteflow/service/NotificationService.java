package com.quoteflow.service;

import com.quoteflow.entity.DemandeDevis;
import com.quoteflow.entity.Notification;
import com.quoteflow.entity.User;
import com.quoteflow.entity.enums.StatutNotification;
import com.quoteflow.repository.NotificationRepository;
import org.springframework.stereotype.Service;

// Persiste une trace de chaque notification declenchee. 
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void tracer(String type, String contenu, User user, DemandeDevis demande, boolean succes) {
        Notification n = Notification.builder()
                .type(type)
                .destinataire(user != null ? user.getEmail() : "")
                .contenu(contenu)
                .statut(succes ? StatutNotification.ENVOYE : StatutNotification.ECHOUE)
                .user(user)
                .demande(demande)
                .build();
        notificationRepository.save(n);
    }
}
