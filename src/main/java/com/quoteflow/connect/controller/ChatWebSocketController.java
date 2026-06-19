package com.quoteflow.connect.controller;

import com.quoteflow.connect.dto.SendMessageRequest;
import com.quoteflow.connect.service.CurrentUserProvider;
import com.quoteflow.connect.service.MessageService;
import com.quoteflow.entity.User; // <-- adapter
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Point d'entrÃ©e STOMP pour les messages texte temps rÃ©el.
 * Le client publie sur : /app/conversation/{id}/send
 * Le service rediffuse ensuite sur : /topic/conversation/{id}
 * (la diffusion est gÃ©rÃ©e dans MessageService via SimpMessagingTemplate).
 */
@Controller
public class ChatWebSocketController {

    private final MessageService messageService;
    private final CurrentUserProvider currentUser;

    public ChatWebSocketController(MessageService messageService, CurrentUserProvider currentUser) {
        this.messageService = messageService;
        this.currentUser = currentUser;
    }

    @MessageMapping("/conversation/{conversationId}/send")
    public void send(@DestinationVariable Long conversationId,
                     @Payload SendMessageRequest req,
                     Principal principal) {
        User expediteur = currentUser.fromPrincipal(principal);
        // Pas de fichiers via WebSocket : texte uniquement (les fichiers passent en REST multipart)
        messageService.send(conversationId, expediteur, req.contenu(), null);
    }
}

