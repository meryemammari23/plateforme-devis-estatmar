package com.quoteflow.auth;

import com.quoteflow.entity.Role;
import com.quoteflow.entity.User;
import com.quoteflow.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Crée 3 comptes de test au démarrage (base H2 recréée à chaque lancement).
 * Pratique pour tester login/JWT avec chaque rôle. À retirer en production.
 *
 *   admin@quoteflow.com    / password   (ADMIN)
 *   employe@quoteflow.com  / password   (EMPLOYE)
 *   client@quoteflow.com   / password   (CLIENT)
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedUsers(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            creer(repo, encoder, "Admin", "QuoteFlow", "admin@quoteflow.com", Role.ADMIN);
            creer(repo, encoder, "Emma", "Employe", "employe@quoteflow.com", Role.EMPLOYE);
            creer(repo, encoder, "Chris", "Client", "client@quoteflow.com", Role.CLIENT);
        };
    }

    private void creer(UserRepository repo, PasswordEncoder encoder,
                       String prenom, String nom, String email, Role role) {
        if (repo.findByEmail(email).isPresent()) return;
        User u = new User();
        u.setPrenom(prenom);
        u.setNom(nom);
        u.setEmail(email);
        u.setPassword(encoder.encode("password"));
        u.setRole(role);
        u.setActif(true);
        repo.save(u);
    }
}
