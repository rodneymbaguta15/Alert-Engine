package com.alert_engine.dto;


import java.time.Instant;
import java.util.List;

/**
 * Standard error response shape. Every 4xx/5xx from this API looks the same —
 * simplifies frontend error handling.
 * <p>
 * fieldErrors is only populated for 400 validation failures. Null otherwise.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {}

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, null);
    }

    public static ErrorResponse withFieldErrors(int status, String error, String message,
                                                String path, List<FieldError> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, path, fieldErrors);
    }
}
