package com.alert_engine.exception;

/**
 * Thrown when a requested entity doesn't exist (or isn't visible to the current user).
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
