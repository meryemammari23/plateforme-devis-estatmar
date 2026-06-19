package com.quoteflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Le nom est obligatoire") String nom,
        @NotBlank(message = "Le prénom est obligatoire") String prenom,
        @NotBlank @Email(message = "Email invalide") String email,
        @NotBlank @Size(min = 6, message = "Mot de passe : 6 caractères minimum") String password
) {}
