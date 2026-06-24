package com.quoteflow.controller;

import com.quoteflow.dto.devis.DevisRequest;
import com.quoteflow.dto.devis.DevisResponse;
import com.quoteflow.security.CustomUserDetails;
import com.quoteflow.service.DevisService;
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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

// Devis et telechargement du PDF 

@RestController
@RequestMapping("/api/devis")
@Tag(name = "Devis", description = "Redaction des devis et telechargement du PDF")
public class DevisController {

    private final DevisService devisService;

    public DevisController(DevisService devisService) {
        this.devisService = devisService;
    }

    @Operation(summary = "Creer et soumettre un devis (employe)")
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYE')")
    public ResponseEntity<DevisResponse> creer(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody DevisRequest request) {
        Long employeId = principal.getUser().getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(devisService.creerEtValider(employeId, request));
    }

    @Operation(summary = "Voir un devis")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYE','CLIENT')")
    public ResponseEntity<DevisResponse> voir(@PathVariable Long id) {
        return ResponseEntity.ok(devisService.voir(id));
    }

    @Operation(summary = "Telecharger le PDF du devis")
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYE','CLIENT')")
    public ResponseEntity<Resource> telechargerPdf(@PathVariable Long id) {
        String chemin = devisService.cheminPdf(id);
        Path path = Paths.get(chemin);
        File file = path.toFile();
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    }
}
