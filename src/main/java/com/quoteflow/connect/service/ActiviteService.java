package com.quoteflow.connect.service;

import com.quoteflow.connect.entity.Activite;
import com.quoteflow.connect.entity.ActiviteType;
import com.quoteflow.connect.repository.ActiviteRepository;
import com.quoteflow.entity.DemandeDevis; // <-- adapter
import com.quoteflow.entity.User;          // <-- adapter
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Journalise les Ã©vÃ©nements mÃ©tier dans la timeline.
 * Ã€ appeler depuis les modules existants (attribution, validation devis, etc.).
 */
@Service
public class ActiviteService {

    private final ActiviteRepository repo;

    public ActiviteService(ActiviteRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Activite log(DemandeDevis demande, ActiviteType type, User acteur, String description) {
        Activite a = new Activite();
        a.setDemande(demande);
        a.setType(type);
        a.setActeur(acteur);
        a.setDescription(description);
        return repo.save(a);
    }
}

