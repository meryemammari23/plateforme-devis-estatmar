package com.quoteflow.entity;

/**
 * Statuts du cycle de vie d'une demande.
 * Les libellés sont sans accent pour correspondre exactement aux
 * valeurs attendues par les webhooks n8n (EN_COURS / VALIDE / REFUSE).
 */
public enum StatutDemande {
    EN_ATTENTE,
    EN_COURS,
    VALIDE,
    REFUSE
}
