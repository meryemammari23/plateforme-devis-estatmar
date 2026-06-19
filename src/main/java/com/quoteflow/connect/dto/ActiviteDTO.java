package com.quoteflow.connect.dto;

import java.time.Instant;

public record ActiviteDTO(
        Long id,
        String type,
        String description,
        Long acteurId,
        String acteurNom,
        Instant dateCreation
) {}

