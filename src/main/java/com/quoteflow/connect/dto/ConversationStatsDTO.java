package com.quoteflow.connect.dto;

import java.time.Instant;

/** Indicateurs de la colonne droite (dashboard conversation). */
public record ConversationStatsDTO(
        long nombreMessages,
        long nombreFichiers,
        Long tempsMoyenReponseSecondes, // null si non calculable
        Instant derniereActivite
) {}

