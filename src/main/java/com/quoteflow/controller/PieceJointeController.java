package com.quoteflow.controller;

import com.quoteflow.entity.PieceJointe;
import com.quoteflow.service.PieceJointeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/demandes")
@Tag(name = "Pieces jointes", description = "Upload de fichiers lies a une demande (Meryem)")
public class PieceJointeController {

    private final PieceJointeService pieceJointeService;

    public PieceJointeController(PieceJointeService pieceJointeService) {
        this.pieceJointeService = pieceJointeService;
    }

    @Operation(summary = "Uploader une piece jointe pour une demande")
    @PostMapping(value = "/{demandeId}/pieces-jointes", consumes = "multipart/form-data")
    public ResponseEntity<PieceJointe> upload(@PathVariable Long demandeId,
                                              @RequestParam("fichier") MultipartFile fichier) {
        return ResponseEntity.ok(pieceJointeService.upload(demandeId, fichier));
    }
}
