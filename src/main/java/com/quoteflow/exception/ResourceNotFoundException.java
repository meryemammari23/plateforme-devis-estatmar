package com.quoteflow.exception;

// Levee quand une ressource demandee n'existe pas (HTTP 404)

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
