package com.quoteflow.auth;

import com.quoteflow.auth.dto.AuthResponse;
import com.quoteflow.auth.dto.LoginRequest;
import com.quoteflow.auth.dto.RegisterRequest;
import com.quoteflow.entity.Role;
import com.quoteflow.entity.User;
import com.quoteflow.repository.UserRepository;
import com.quoteflow.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /** Inscription d'un client (rôle CLIENT par défaut). */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.findByEmail(req.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un compte existe déjà avec cet email");
        }
        User user = new User();
        user.setNom(req.nom());
        user.setPrenom(req.prenom());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRole(Role.CLIENT);
        user.setActif(true);
        return reponse(userRepository.save(user));
    }

    /** Connexion : vérifie les identifiants et renvoie un token. */
    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect");
        }
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides"));
        if (!user.isActif()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Compte désactivé");
        }
        return reponse(user);
    }

    private AuthResponse reponse(User user) {
        String token = jwtService.genererToken(user.getEmail(), user.getId(), user.getRole().name());
        return new AuthResponse(
                token, user.getId(), user.getNom(), user.getPrenom(),
                user.getEmail(), user.getRole().name());
    }
}
