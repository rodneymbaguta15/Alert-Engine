package com.alert_engine.exception;

import com.alert_engine.dto.ErrorResponse;
import com.alert_engine.dto.ErrorResponse.FieldError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Centralizes HTTP error mapping so controllers can just throw domain exceptions.
 * Every handler returns the same ErrorResponse shape for a uniform API contract.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** 404 for "entity not found / not visible to you." */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, "Not Found", ex.getMessage(), req.getRequestURI()));
    }

    /** 400 for domain-specific validation — e.g., ticker not in whitelist. */
    @ExceptionHandler(InvalidTickerException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTicker(InvalidTickerException ex,
                                                             HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, "Bad Request", ex.getMessage(), req.getRequestURI()));
    }

    /** 400 for @Valid failures on request DTOs — renders each field error. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        return ResponseEntity.badRequest()
                .body(ErrorResponse.withFieldErrors(
                        400, "Validation Failed", "One or more fields are invalid",
                        req.getRequestURI(), fieldErrors));
    }

    /** 400 for malformed JSON — unparseable body, wrong enum value, etc. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex,
                                                             HttpServletRequest req) {
        // ex.getMessage() can be noisy (mentions internal class names). Keep a generic message.
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, "Bad Request",
                        "Request body is malformed or contains invalid values",
                        req.getRequestURI()));
    }

    /** 400 for bean-validation failures wrapped as IllegalArgumentException (e.g., from services). */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of(400, "Bad Request", ex.getMessage(), req.getRequestURI()));
    }

    /** 500 catch-all. Logs the full stack trace so it's visible server-side. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {}", req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "Internal Server Error",
                        "An unexpected error occurred", req.getRequestURI()));
    }
 /** Authentication failures are handled separately in AuthenticationEntryPointImpl to ensure */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex,
                                                              HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(401, "Unauthorized", ex.getMessage(), req.getRequestURI()));
    }
}