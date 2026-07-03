package com.estatmar.devis.dto;

import lombok.*;

/**
 * Reponse renvoyee au frontend apres inscription ou connexion reussie :
 * le token JWT + les infos de base de l'utilisateur.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
}