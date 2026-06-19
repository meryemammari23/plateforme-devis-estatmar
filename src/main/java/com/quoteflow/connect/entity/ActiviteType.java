package com.quoteflow.connect.entity;

/**
 * Types d'Ã©vÃ©nements affichÃ©s dans la timeline d'une demande.
 * SÃ©pare l'historique mÃ©tier (Activite) de la discussion (Message).
 */
public enum ActiviteType {
    DEMANDE_SOUMISE,
    DOSSIER_ATTRIBUE,
    INFO_DEMANDEE,
    DOCUMENT_AJOUTE,
    MESSAGE_ENVOYE,
    DEVIS_GENERE,
    DEVIS_VALIDE,
    DEVIS_REFUSE,
    STATUT_CHANGE
}

