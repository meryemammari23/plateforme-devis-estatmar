package com.quoteflow.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// Reponse renvoyee apres login/register : le token + infos d'affichage. 

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String nom;
    private String prenom;
    private String role;
}
