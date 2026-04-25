package com.alert_engine.exception;

/**
 * Thrown when a request references a ticker that isn't in the allowed whitelist.
 * Mapped to HTTP 400.
 */

public class InvalidTickerException extends RuntimeException {
    public InvalidTickerException(String message) {
        super(message);
    }
}
