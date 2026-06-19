package com.quoteflow.connect.controller;

import com.quoteflow.connect.entity.MessageAttachment;
import com.quoteflow.connect.repository.MessageAttachmentRepository;
import com.quoteflow.connect.service.ConversationService;
import com.quoteflow.connect.service.CurrentUserProvider;
import com.quoteflow.connect.service.FileStorageService;
import com.quoteflow.entity.User; // <-- adapter
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "QuoteFlow Connect â€“ Fichiers")
@RestController
@RequestMapping("/api/connect/files")
@PreAuthorize("hasAnyRole('CLIENT','EMPLOYE','ADMIN')")
public class FileController {

    private final MessageAttachmentRepository attachmentRepo;
    private final FileStorageService storage;
    private final ConversationService conversationService;
    private final CurrentUserProvider currentUser;

    public FileController(MessageAttachmentRepository attachmentRepo,
                          FileStorageService storage,
                          ConversationService conversationService,
                          CurrentUserProvider currentUser) {
        this.attachmentRepo = attachmentRepo;
        this.storage = storage;
        this.conversationService = conversationService;
        this.currentUser = currentUser;
    }

    @Operation(summary = "TÃ©lÃ©charger une piÃ¨ce jointe (contrÃ´le d'accÃ¨s Ã  la conversation)")
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long attachmentId, Authentication auth) {
        MessageAttachment att = attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("PiÃ¨ce jointe introuvable"));

        // VÃ©rifie que l'utilisateur a bien accÃ¨s Ã  la conversation propriÃ©taire du fichier
        User u = currentUser.fromAuthentication(auth);
        Long conversationId = att.getMessage().getConversation().getId();
        conversationService.loadAuthorized(conversationId, u);

        Resource resource = storage.load(att.getChemin());
        String mime = att.getTypeMime() != null ? att.getTypeMime()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + att.getNomFichier() + "\"")
                .body(resource);
    }
}

