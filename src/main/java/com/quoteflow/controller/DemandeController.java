package com.quoteflow.controller;

import com.quoteflow.dto.DemandeRequest;
import com.quoteflow.entity.DemandeDevis;
import com.quoteflow.service.DemandePdfService;
import com.quoteflow.service.DemandeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demandes")
@Tag(name = "Demandes", description = "Module client - demandes de devis (Meryem)")
public class DemandeController {

    private final DemandeService demandeService;
    private final DemandePdfService demandePdfService;

    public DemandeController(DemandeService demandeService,
                            DemandePdfService demandePdfService) {
        this.demandeService = demandeService;
        this.demandePdfService = demandePdfService;
    }

    @Operation(summary = "Soumettre une nouvelle demande (statut EN_ATTENTE + email confirmation)")
    @PostMapping
    public ResponseEntity<DemandeDevis> creer(@Valid @RequestBody DemandeRequest req) {
        return ResponseEntity.ok(demandeService.creer(req));
    }

    @Operation(summary = "Lister toutes les demandes")
    @GetMapping
    public List<DemandeDevis> toutes() {
        return demandeService.findAll();
    }

    @Operation(summary = "Mes demandes (par clientId, pour le test local)")
    @GetMapping("/mes-demandes")
    public List<DemandeDevis> mesDemandes(@RequestParam Long clientId) {
        return demandeService.findByClient(clientId);
    }

    @Operation(summary = "Detail d'une demande")
    @GetMapping("/{id}")
    public DemandeDevis detail(@PathVariable Long id) {
        return demandeService.findById(id);
    }

    @Operation(summary = "Attribuer la demande a un employe (statut EN_COURS + email)")
    @PutMapping("/{id}/attribuer")
    public ResponseEntity<DemandeDevis> attribuer(@PathVariable Long id,
                                                  @RequestParam(required = false) Long employeId) {
        return ResponseEntity.ok(demandeService.attribuer(id, employeId));
    }

    @Operation(summary = "Telecharger le recap PDF de la demande (destine a l'employe)")
    @GetMapping("/{id}/recap-pdf")
    public ResponseEntity<byte[]> recapPdf(@PathVariable Long id) {
        DemandeDevis demande = demandeService.findById(id);
        byte[] pdf = demandePdfService.genererRecap(demande);
        String nom = "recap-" + demande.getReference() + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nom + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
