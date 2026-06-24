package com.quoteflow.service;

import com.quoteflow.dto.auth.AuthResponse;
import com.quoteflow.dto.auth.LoginRequest;
import com.quoteflow.dto.auth.RegisterRequest;
import com.quoteflow.entity.User;
import com.quoteflow.entity.enums.Role;
import com.quoteflow.exception.BusinessException;
import com.quoteflow.repository.UserRepository;
import com.quoteflow.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Inscription des clients et authentification (delivrance du JWT). 

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    // Inscription d'un nouveau client (role CLIENT force). 

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Un compte existe deja avec cet email");
        }
        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CLIENT)
                .actif(true)
                .build();
        userRepository.save(user);
        return buildResponse(user);
    }

    // Connexion : verifie les identifiants puis renvoie un token. 
    
    public AuthResponse login(LoginRequest request) {
        // Leve BadCredentialsException si invalide (geree par le handler global).
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

        if (!user.isActif()) {
            throw new BusinessException("Ce compte est desactive");
        }
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        return AuthResponse.builder()
                .token(jwtUtil.generateToken(user))
                .userId(user.getId())
                .email(user.getEmail())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .role(user.getRole().name())
                .build();
    }
}
