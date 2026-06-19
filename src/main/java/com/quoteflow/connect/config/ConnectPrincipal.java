package com.quoteflow.connect.config;

import java.security.Principal;

/**
 * Principal attachÃ© Ã  la session WebSocket aprÃ¨s validation du JWT.
 * getName() renvoie l'ID utilisateur (String) : c'est la clÃ© utilisÃ©e par
 * Spring pour router les messages /user/{id}/queue/...
 */
public record ConnectPrincipal(Long userId, String email, String role) implements Principal {
    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}

