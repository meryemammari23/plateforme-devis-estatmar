package com.quoteflow.controller;

import com.quoteflow.dto.demande.DemandeRequest;
import com.quoteflow.dto.demande.DemandeResponse;
import com.quoteflow.entity.PieceJointe;
import com.quoteflow.security.CustomUserDetails;
import com.quoteflow.service.DemandeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

// Demandes de devis 
 
@RestController
@RequestMapping("/api/demandes")
@Tag(name = "Demandes", description = "Soumission et suivi des demandes de devis")
public class DemandeController {

    private final DemandeService demandeService;

    public DemandeController(DemandeService demandeService) {
        this.demandeService = demandeService;
    }

    @Operation(summary = "Soumettre une nouvelle demande (client connecte)")
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DemandeResponse> soumettre(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody DemandeRequest request) {
        Long clientId = principal.getUser().getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(demandeService.soumettre(clientId, request));
    }

    @Operation(summary = "Mes demandes (client connecte)")
    @GetMapping("/mes-demandes")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<DemandeResponse>> mesDemandes(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(demandeService.mesDemandes(principal.getUser().getId()));
    }

    @Operation(summary = "Toutes les demandes (admin / employe)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYE')")
    public ResponseEntity<List<DemandeResponse>> toutes() {
        return ResponseEntity.ok(demandeService.toutesLesDemandes());
    }

    @Operation(summary = "Detail d'une demande")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYE','CLIENT')")
    public ResponseEntity<DemandeResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(demandeService.detail(id));
    }

    @Operation(summary = "Joindre des fichiers PDF a une demande (client)")
    @PostMapping(value = "/{id}/pieces-jointes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<DemandeResponse> ajouterPiecesJointes(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @RequestParam("fichiers") MultipartFile[] fichiers) {
        Long clientId = principal.getUser().getId();
        return ResponseEntity.ok(demandeService.ajouterPiecesJointes(id, clientId, fichiers));
    }

    @Operation(summary = "Telecharger une piece jointe")
    @GetMapping("/pieces-jointes/{pieceJointeId}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYE','CLIENT')")
    public ResponseEntity<Resource> telechargerPieceJointe(@PathVariable Long pieceJointeId) {
        PieceJointe pj = demandeService.getPieceJointe(pieceJointeId);
        File file = new File(pj.getCheminFichier());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + pj.getNomOriginal() + "\"")
                .body(new FileSystemResource(file));
    }

    @Operation(summary = "Demandes en attente depuis plus de X jours (pour rappel n8n)")
    @GetMapping("/en-retard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<com.quoteflow.dto.demande.RappelResponse>> demandesEnRetard(
            @RequestParam(name = "jours", defaultValue = "3") int jours) {
        return ResponseEntity.ok(demandeService.demandesEnRetard(jours));
    }

    @Operation(summary = "Refuser une demande (client / admin)")
    @PutMapping("/{id}/refuser")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    public ResponseEntity<DemandeResponse> refuser(@PathVariable Long id) {
        return ResponseEntity.ok(demandeService.refuser(id));
    }
}
