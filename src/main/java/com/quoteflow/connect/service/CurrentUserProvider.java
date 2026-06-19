package com.quoteflow.connect.service;

import com.quoteflow.connect.config.ConnectPrincipal;
import com.quoteflow.entity.User;              // <-- adapter
import com.quoteflow.repository.UserRepository; // <-- adapter
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * RÃ©sout le User courant Ã  partir du contexte de sÃ©curitÃ©, que la requÃªte
 * vienne du REST (Authentication, name = email) ou du WebSocket
 * (ConnectPrincipal, name = userId). TolÃ©rant Ã  votre implÃ©mentation de
 * UserDetails : on s'appuie sur l'email/username, pas sur le type du principal.
 */
@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** Contexte REST : Spring Security fournit l'Authentication. */
    public User fromAuthentication(Authentication auth) {
        if (auth == null || !auth.isAuthenticated())
            throw new IllegalStateException("Utilisateur non authentifiÃ©");
        String email = auth.getName(); // par dÃ©faut = username = email
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable : " + email));
    }

    /** Contexte WebSocket : le principal est un ConnectPrincipal (name = userId). */
    public User fromPrincipal(Principal principal) {
        if (principal instanceof ConnectPrincipal cp) {
            return userRepository.findById(cp.userId())
                    .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
        }
        // Repli : principal REST classique
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable"));
    }
}

