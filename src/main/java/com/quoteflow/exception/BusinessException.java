package com.quoteflow.exception;

// Levee pour une regle metier violee (HTTP 400)

public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
