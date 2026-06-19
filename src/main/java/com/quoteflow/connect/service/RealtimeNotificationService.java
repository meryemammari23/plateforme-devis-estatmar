package com.quoteflow.connect.service;

import com.quoteflow.connect.dto.MessageDTO;
import com.quoteflow.connect.dto.RealtimeNotification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Diffusion temps rÃ©el via STOMP.
 *  - Messages d'une conversation : /topic/conversation/{id}
 *  - Notifications personnelles  : /user/{userId}/queue/notifications
 * Le prÃ©fixe /user permet Ã  Spring de router vers la session de l'utilisateur ciblÃ©.
 */
@Service
public class RealtimeNotificationService {

    private final SimpMessagingTemplate messaging;

    public RealtimeNotificationService(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /** Pousse un nouveau message Ã  tous les abonnÃ©s de la conversation. */
    public void broadcastMessage(Long conversationId, MessageDTO message) {
        messaging.convertAndSend("/topic/conversation/" + conversationId, message);
    }

    /** Notifie un utilisateur prÃ©cis (badge, toast). */
    public void notifyUser(Long userId, RealtimeNotification notif) {
        messaging.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/notifications",
                notif
        );
    }

    /** Signale que des messages ont Ã©tÃ© lus (mise Ã  jour des Â« Vu Â» cÃ´tÃ© expÃ©diteur). */
    public void broadcastRead(Long conversationId, Long lecteurId) {
        messaging.convertAndSend("/topic/conversation/" + conversationId + "/read", lecteurId);
    }
}

