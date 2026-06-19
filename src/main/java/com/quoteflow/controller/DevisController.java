package com.quoteflow.controller;

import com.quoteflow.dto.DevisRequest;
import com.quoteflow.entity.Devis;
import com.quoteflow.service.DevisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/devis")
@Tag(name = "Devis", description = "Redaction du devis + generation PDF + envoi n8n (Meryem)")
public class DevisController {

    private final DevisService devisService;

    public DevisController(DevisService devisService) {
        this.devisService = devisService;
    }

    @Operation(summary = "Creer et envoyer un devis (genere le PDF, passe la demande a VALIDE, email client)")
    @PostMapping
    public ResponseEntity<Devis> creer(@Valid @RequestBody DevisRequest req) {
        return ResponseEntity.ok(devisService.creer(req));
    }

    @Operation(summary = "Voir un devis")
    @GetMapping("/{id}")
    public Devis detail(@PathVariable Long id) {
        return devisService.findById(id);
    }

    @Operation(summary = "Telecharger le PDF du devis")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        byte[] pdf = devisService.getPdfBytes(id);
        Devis devis = devisService.findById(id);
        String ref = devis.getDemande() != null ? devis.getDemande().getReference()
                : String.valueOf(id);
        String nom = "devis-" + ref + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nom + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
