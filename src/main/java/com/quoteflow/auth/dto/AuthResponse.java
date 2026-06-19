package com.quoteflow.auth.dto;

/** Réponse renvoyée après register/login : le token + infos utilisateur de base. */
public record AuthResponse(
        String token,
        Long userId,
        String nom,
        String prenom,
        String email,
        String role
) {}
