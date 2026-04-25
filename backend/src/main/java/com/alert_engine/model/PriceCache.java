package com.alert_engine.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Latest known quote per ticker — one row per symbol, upserted on each poll.
 * Serves the UI (so the frontend doesn't hit Finnhub directly) and the
 * evaluator (so it has something to compare against thresholds).
 */
@Entity
@Table(name = "price_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceCache {

    // Ticker is the natural primary key — one row per symbol.
    @Id
    @Column(length = 10)
    private String ticker;

    @Column(name = "current_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "previous_close", precision = 18, scale = 4)
    private BigDecimal previousClose;

    @Column(name = "high_price", precision = 18, scale = 4)
    private BigDecimal highPrice;

    @Column(name = "low_price", precision = 18, scale = 4)
    private BigDecimal lowPrice;

    @Column(name = "open_price", precision = 18, scale = 4)
    private BigDecimal openPrice;

    // The timestamp Finnhub reports for the quote itself.
    @Column(name = "quote_timestamp")
    private Instant quoteTimestamp;

    // When *we* fetched it. Written by the app on upsert, not by the DB default.
    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;
}