package com.quoteflow.connect.controller;

import com.quoteflow.connect.dto.MessageDTO;
import com.quoteflow.connect.service.CurrentUserProvider;
import com.quoteflow.connect.service.MessageService;
import com.quoteflow.entity.User; // <-- adapter
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Voie REST pour l'envoi de messages â€” notamment ceux AVEC piÃ¨ces jointes
 * (le multipart ne passe pas par STOMP). Les messages texte purs peuvent aussi
 * passer par WebSocket (voir ChatWebSocketController) pour le temps rÃ©el.
 */
@Tag(name = "QuoteFlow Connect â€“ Messages")
@RestController
@RequestMapping("/api/connect/conversations/{conversationId}")
@PreAuthorize("hasAnyRole('CLIENT','EMPLOYE','ADMIN')")
public class MessageRestController {

    private final MessageService messageService;
    private final CurrentUserProvider currentUser;

    public MessageRestController(MessageService messageService, CurrentUserProvider currentUser) {
        this.messageService = messageService;
        this.currentUser = currentUser;
    }

    @Operation(summary = "Envoyer un message (texte et/ou fichiers)")
    @PostMapping(value = "/messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MessageDTO send(@PathVariable Long conversationId,
                           @RequestParam(value = "contenu", required = false) String contenu,
                           @RequestParam(value = "fichiers", required = false) List<MultipartFile> fichiers,
                           Authentication auth) {
        User u = currentUser.fromAuthentication(auth);
        return messageService.send(conversationId, u, contenu, fichiers);
    }

    @Operation(summary = "Marquer la conversation comme lue")
    @PostMapping("/read")
    public ResponseEntity<Map<String, Integer>> markRead(@PathVariable Long conversationId,
                                                         Authentication auth) {
        User u = currentUser.fromAuthentication(auth);
        int n = messageService.markAsRead(conversationId, u);
        return ResponseEntity.ok(Map.of("messagesMarques", n));
    }
}

