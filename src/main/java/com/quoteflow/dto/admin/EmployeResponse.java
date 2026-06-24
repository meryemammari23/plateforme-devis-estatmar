package com.quoteflow.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmployeResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private boolean actif;
    private LocalDateTime dateCreation;
}
