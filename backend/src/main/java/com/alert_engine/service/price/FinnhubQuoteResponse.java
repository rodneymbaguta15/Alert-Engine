package com.alert_engine.service.price;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Raw response from Finnhub's /quote endpoint.
 * <p>
 * Fields are cryptic one-letter keys — we map them to readable names via @JsonProperty.
 * Using BigDecimal, not double. Money should be exact.
 * <p>
 * For invalid symbols or off-market hours, fields may be null or zero — callers must guard.
 */
@Getter
@Setter
@ToString
public class FinnhubQuoteResponse {

    @JsonProperty("c")
    private BigDecimal currentPrice;

    @JsonProperty("d")
    private BigDecimal change;

    @JsonProperty("dp")
    private BigDecimal changePercent;

    @JsonProperty("h")
    private BigDecimal highPrice;

    @JsonProperty("l")
    private BigDecimal lowPrice;

    @JsonProperty("o")
    private BigDecimal openPrice;

    @JsonProperty("pc")
    private BigDecimal previousClose;

    /** Unix timestamp in seconds (not millis). Convert via Instant.ofEpochSecond(t). */
    @JsonProperty("t")
    private Long timestamp;

    /**
     * Finnhub returns c=0 for unknown/invalid symbols rather than an error response.
     * Treat a zero or null current price as "no valid data."
     */
    public boolean hasValidQuote() {
        return currentPrice != null && currentPrice.signum() > 0;
    }
}