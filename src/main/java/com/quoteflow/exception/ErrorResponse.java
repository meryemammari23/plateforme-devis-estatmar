package com.quoteflow.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

// Format unifie des reponses d'erreur de l'API
 
@Getter
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    /** Detail des erreurs de validation par champ (null sinon). */
    private Map<String, String> validationErrors;
}
