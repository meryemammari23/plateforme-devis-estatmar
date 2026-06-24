package com.quoteflow.service;

import com.quoteflow.entity.User;
import com.quoteflow.entity.enums.Role;
import com.quoteflow.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Garantit l'existence d'un compte administrateur fonctionnel a chaque demarrage.
 * - S'il n'existe pas : il est cree.
 * - S'il existe deja : son role, son etat actif et son mot de passe sont
 *   re-synchronises sur les valeurs de configuration (le hash est toujours
 *   genere par l'application, donc valide).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        User admin = userRepository.findByEmail(adminEmail).orElse(null);

        if (admin == null) {
            admin = User.builder()
                    .nom("Admin").prenom("QuoteFlow")
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN).actif(true)
                    .build();
            userRepository.save(admin);
            log.info("Compte admin cree : {} (mot de passe : {})", adminEmail, adminPassword);
        } else {
            // Re-synchronise pour eviter tout compte admin inaccessible.
            admin.setRole(Role.ADMIN);
            admin.setActif(true);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);
            log.info("Compte admin verifie/reinitialise : {} (mot de passe : {})", adminEmail, adminPassword);
        }
    }
}
