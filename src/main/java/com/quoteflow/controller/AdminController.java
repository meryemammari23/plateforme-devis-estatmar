package com.quoteflow.controller;

import com.quoteflow.dto.admin.EmployeRequest;
import com.quoteflow.dto.admin.EmployeResponse;
import com.quoteflow.dto.demande.DemandeResponse;
import com.quoteflow.service.DemandeService;
import com.quoteflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Gestion des employes et attribution des dossiers 
 
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Gestion des employes et attribution")
public class AdminController {

    private final UserService userService;
    private final DemandeService demandeService;

    public AdminController(UserService userService, DemandeService demandeService) {
        this.userService = userService;
        this.demandeService = demandeService;
    }

    @Operation(summary = "Creer un compte employe")
    @PostMapping("/employes")
    public ResponseEntity<EmployeResponse> creerEmploye(@Valid @RequestBody EmployeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.creerEmploye(request));
    }

    @Operation(summary = "Lister les employes")
    @GetMapping("/employes")
    public ResponseEntity<List<EmployeResponse>> listerEmployes() {
        return ResponseEntity.ok(userService.listerEmployes());
    }

    @Operation(summary = "Activer / desactiver un employe")
    @PutMapping("/employes/{id}/activer")
    public ResponseEntity<EmployeResponse> changerActivation(@PathVariable Long id) {
        return ResponseEntity.ok(userService.changerActivation(id));
    }

    @Operation(summary = "Attribuer un dossier a un employe")
    @PutMapping("/demandes/{id}/attribuer")
    public ResponseEntity<DemandeResponse> attribuer(@PathVariable Long id,
                                                     @RequestParam Long employeId) {
        return ResponseEntity.ok(demandeService.attribuer(id, employeId));
    }
}
