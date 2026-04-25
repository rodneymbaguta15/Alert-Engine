package com.alert_engine.dto;


import com.alert_engine.model.PriceCache;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Dashboard/UI response for current prices. Backed by price_cache (populated
 * by the scheduled poller), NOT a live Finnhub call — that way the UI is fast
 * and we don't burn API quota on every page load.
 */
public record PriceQuoteResponse(
        String ticker,
        BigDecimal currentPrice,
        BigDecimal previousClose,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal openPrice,
        Instant quoteTimestamp,
        Instant fetchedAt
) {
    public static PriceQuoteResponse from(PriceCache cache) {
        return new PriceQuoteResponse(
                cache.getTicker(),
                cache.getCurrentPrice(),
                cache.getPreviousClose(),
                cache.getHighPrice(),
                cache.getLowPrice(),
                cache.getOpenPrice(),
                cache.getQuoteTimestamp(),
                cache.getFetchedAt()
        );
    }
}