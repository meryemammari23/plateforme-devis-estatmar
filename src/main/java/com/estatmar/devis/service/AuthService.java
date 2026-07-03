package com.estatmar.devis.service;

import com.estatmar.devis.dto.AuthResponse;
import com.estatmar.devis.dto.LoginRequest;
import com.estatmar.devis.dto.RegisterRequest;
import com.estatmar.devis.entity.Role;
import com.estatmar.devis.entity.Utilisateur;
import com.estatmar.devis.repository.UtilisateurRepository;
import com.estatmar.devis.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service d'authentification : inscription des clients et connexion de tous les roles.
 *
 * Regle metier : l'inscription publique cree toujours un compte CLIENT.
 * Les comptes EMPLOYE sont crees par l'ADMIN (module de Meryem).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est deja utilise : " + request.getEmail());
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .telephone(request.getTelephone())
                .role(Role.CLIENT)
                .build();

        utilisateurRepository.save(utilisateur);

        return buildAuthResponse(utilisateur);
    }

    public AuthResponse login(LoginRequest request) {
        // Leve une exception si email/mot de passe incorrects
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getMotDePasse()));

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow();

        return buildAuthResponse(utilisateur);
    }

    private AuthResponse buildAuthResponse(Utilisateur utilisateur) {
        String token = jwtService.generateToken(utilisateur);
        return AuthResponse.builder()
                .token(token)
                .id(utilisateur.getId())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole().name())
                .build();
    }
}