package com.quoteflow.entity.enums;

// Cycle de vie d'une demande de devis (transitions automatiques). 

public enum StatutDemande {
    EN_ATTENTE,   // Client a soumis la demande
    EN_COURS,     // Admin a attribue a un employe
    VALIDE,       // Employe a valide et envoye le devis
    REFUSE        // Client ou Admin a refuse
}
