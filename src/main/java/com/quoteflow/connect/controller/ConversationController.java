package com.quoteflow.connect.controller;

import com.quoteflow.connect.dto.*;
import com.quoteflow.connect.entity.Conversation;
import com.quoteflow.connect.service.ConversationService;
import com.quoteflow.connect.service.CurrentUserProvider;
import com.quoteflow.entity.User; // <-- adapter
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "QuoteFlow Connect â€“ Conversations")
@RestController
@RequestMapping("/api/connect")
@PreAuthorize("hasAnyRole('CLIENT','EMPLOYE','ADMIN')")
public class ConversationController {

    private final ConversationService conversationService;
    private final CurrentUserProvider currentUser;

    public ConversationController(ConversationService conversationService,
                                  CurrentUserProvider currentUser) {
        this.conversationService = conversationService;
        this.currentUser = currentUser;
    }

    @Operation(summary = "Lister les espaces collaboratifs de l'utilisateur connectÃ©")
    @GetMapping("/conversations")
    public List<ConversationSummaryDTO> list(Authentication auth) {
        return conversationService.listForUser(currentUser.fromAuthentication(auth));
    }

    @Operation(summary = "DÃ©tail d'un espace : messages + timeline")
    @GetMapping("/conversations/{id}")
    public ConversationDetailDTO detail(@PathVariable Long id, Authentication auth) {
        return conversationService.getDetail(id, currentUser.fromAuthentication(auth));
    }

    @Operation(summary = "Statistiques d'une conversation (dashboard latÃ©ral)")
    @GetMapping("/conversations/{id}/stats")
    public ConversationStatsDTO stats(@PathVariable Long id, Authentication auth) {
        return conversationService.stats(id, currentUser.fromAuthentication(auth));
    }

    @Operation(summary = "RÃ©cupÃ©rer ou crÃ©er l'espace d'une demande")
    @GetMapping("/demandes/{demandeId}/conversation")
    public ResponseEntity<Map<String, Long>> getOrCreate(@PathVariable Long demandeId,
                                                          Authentication auth) {
        User u = currentUser.fromAuthentication(auth);
        Conversation c = conversationService.getOrCreate(demandeId, u);
        return ResponseEntity.ok(Map.of("conversationId", c.getId()));
    }
}

