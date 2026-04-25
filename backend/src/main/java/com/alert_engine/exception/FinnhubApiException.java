package com.alert_engine.exception;
/**
 * Thrown when a call to Finnhub fails — network error, non-2xx response,
 * rate limit, or malformed payload. Distinct from generic RuntimeException
 * so callers (and the global exception handler) can treat API failures
 * differently from programming bugs.
 */
public class FinnhubApiException extends RuntimeException {
    public FinnhubApiException(String message) {
        super(message);
    }
    public FinnhubApiException(String message, Throwable cause) {
        super(message, cause);
    }

}
