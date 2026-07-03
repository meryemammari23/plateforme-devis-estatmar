package com.estatmar.devis.config;

import com.estatmar.devis.entity.Role;
import com.estatmar.devis.entity.Utilisateur;
import com.estatmar.devis.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cree un compte administrateur par defaut au premier demarrage
 * pour permettre a l'equipe de tester les modules proteges.
 *
 * Email : admin@estatmar.com / Mot de passe : Admin2026
 * (a changer en production)
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!utilisateurRepository.existsByEmail("admin@estatmar.com")) {
            Utilisateur admin = Utilisateur.builder()
                    .nom("ESTATMAR")
                    .prenom("Admin")
                    .email("admin@estatmar.com")
                    .motDePasse(passwordEncoder.encode("Admin2026"))
                    .role(Role.ADMIN)
                    .build();
            utilisateurRepository.save(admin);
            System.out.println(">>> Compte admin par defaut cree : admin@estatmar.com");
        }
    }
}