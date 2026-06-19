package com.quoteflow.config;

import com.quoteflow.entity.DemandeDevis;
import com.quoteflow.entity.Role;
import com.quoteflow.entity.StatutDemande;
import com.quoteflow.entity.User;
import com.quoteflow.repository.DemandeDevisRepository;
import com.quoteflow.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Insere des donnees de demonstration au demarrage pour pouvoir tester
 * immediatement (1 client, 1 employe, 1 demande EN_ATTENTE).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final DemandeDevisRepository demandeRepo;

    public DataSeeder(UserRepository userRepo, DemandeDevisRepository demandeRepo) {
        this.userRepo = userRepo;
        this.demandeRepo = demandeRepo;
    }

    @Override
    public void run(String... args) {
        if (userRepo.count() > 0) return;

        User client = new User("Ammari", "Meryem", "client@example.com", Role.CLIENT);
        client = userRepo.save(client);

        User employe = new User("Ouissal", "Employe", "employe@example.com", Role.EMPLOYE);
        userRepo.save(employe);

        DemandeDevis d = new DemandeDevis();
        d.setReference("QF-2026-0001");
        d.setDescription("Refonte du site vitrine de l'entreprise avec espace client.");
        d.setBudget(new BigDecimal("15000.00"));
        d.setStatut(StatutDemande.EN_ATTENTE);
        d.setClient(client);
        demandeRepo.save(d);

        System.out.println("=== Donnees de demo inserees ===");
        System.out.println("Client id=" + client.getId() + " (client@example.com)");
        System.out.println("Demande id=" + d.getId() + " reference=" + d.getReference());
        System.out.println("Swagger : http://localhost:8080/swagger-ui.html");
        System.out.println("Console H2 : http://localhost:8080/h2-console (JDBC: jdbc:h2:mem:quoteflow)");
        System.out.println("=================================");
    }
}
