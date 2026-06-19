package com.quoteflow.service;

import com.quoteflow.dto.DemandeRequest;
import com.quoteflow.entity.DemandeDevis;
import com.quoteflow.entity.Role;
import com.quoteflow.entity.StatutDemande;
import com.quoteflow.entity.User;
import com.quoteflow.exception.ResourceNotFoundException;
import com.quoteflow.repository.DemandeDevisRepository;
import com.quoteflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
public class DemandeService {

    private final DemandeDevisRepository demandeRepo;
    private final UserRepository userRepo;
    private final N8nWebhookService n8n;

    public DemandeService(DemandeDevisRepository demandeRepo,
                          UserRepository userRepo,
                          N8nWebhookService n8n) {
        this.demandeRepo = demandeRepo;
        this.userRepo = userRepo;
        this.n8n = n8n;
    }

    /** Soumission d'une nouvelle demande -> statut EN_ATTENTE + webhook confirmation. */
    public DemandeDevis creer(DemandeRequest req) {
        User client = resoudreClient(req.getClientId());

        DemandeDevis d = new DemandeDevis();
        d.setDescription(req.getDescription());
        d.setBudget(req.getBudget());
        d.setClient(client);
        d.setStatut(StatutDemande.EN_ATTENTE);
        d.setReference(genererReference());
        d.setDateCreation(LocalDateTime.now());
        d.setDateMaj(LocalDateTime.now());

        DemandeDevis saved = demandeRepo.save(d);

        n8n.confirmationDemande(client.getEmail(), client.getNom(), saved.getReference());
        return saved;
    }

    /** Attribution a un employe -> statut EN_COURS + webhook changement de statut. */
    public DemandeDevis attribuer(Long demandeId, Long employeId) {
        DemandeDevis d = findById(demandeId);
        User employe = resoudreEmploye(employeId);

        d.setEmploye(employe);
        d.setStatut(StatutDemande.EN_COURS);
        d.setDateMaj(LocalDateTime.now());
        DemandeDevis saved = demandeRepo.save(d);

        if (saved.getClient() != null) {
            n8n.changementStatut(saved.getClient().getEmail(), saved.getClient().getNom(),
                    saved.getReference(), StatutDemande.EN_COURS.toString());
        }
        return saved;
    }

    public DemandeDevis findById(Long id) {
        return demandeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : " + id));
    }

    public List<DemandeDevis> findAll() {
        return demandeRepo.findAll();
    }

    public List<DemandeDevis> findByClient(Long clientId) {
        return demandeRepo.findByClientId(clientId);
    }

    // --- helpers ---

    private String genererReference() {
        long n = demandeRepo.count() + 1;
        return String.format("QF-%d-%04d", Year.now().getValue(), n);
    }

    private User resoudreClient(Long clientId) {
        if (clientId != null) {
            return userRepo.findById(clientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Client introuvable : " + clientId));
        }
        // Test local : on prend le premier client de demonstration.
        return userRepo.findByRole(Role.CLIENT).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Aucun client de demonstration"));
    }

    private User resoudreEmploye(Long employeId) {
        if (employeId != null) {
            return userRepo.findById(employeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Employe introuvable : " + employeId));
        }
        return userRepo.findByRole(Role.EMPLOYE).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Aucun employe de demonstration"));
    }
}
