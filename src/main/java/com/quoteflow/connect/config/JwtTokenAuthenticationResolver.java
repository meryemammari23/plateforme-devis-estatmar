package com.quoteflow.connect.config;

import com.quoteflow.entity.User;
import com.quoteflow.repository.UserRepository;
import com.quoteflow.security.JwtService;
import org.springframework.stereotype.Component;

/**
 * Relie QuoteFlow Connect au JwtService du module auth.
 * Valide le token, en extrait l'email, charge le User, construit le principal.
 */
@Component
public class JwtTokenAuthenticationResolver implements TokenAuthenticationResolver {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtTokenAuthenticationResolver(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public ConnectPrincipal resolve(String bearerToken) {
        try {
            if (!jwtService.isTokenValid(bearerToken)) return null;

            String email = jwtService.extractUsername(bearerToken);
            if (email == null) return null;

            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) return null;

            String role = user.getRole() != null ? user.getRole().toString() : "CLIENT";
            return new ConnectPrincipal(user.getId(), email, role);
        } catch (Exception e) {
            return null;
        }
    }
}
