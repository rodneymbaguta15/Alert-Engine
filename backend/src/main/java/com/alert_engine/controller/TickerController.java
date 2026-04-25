package com.alert_engine.controller;

import com.alert_engine.config.AppTickerConfig;
import com.alert_engine.dto.PriceQuoteResponse;
import com.alert_engine.exception.InvalidTickerException;
import com.alert_engine.exception.ResourceNotFoundException;
import com.alert_engine.repository.PriceCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Supported tickers and their current (cached) prices.
 * <p>
 * Endpoints:
 *   GET /api/tickers                  -> list of allowed tickers (static config)
 *   GET /api/tickers/{symbol}/quote   -> latest cached quote for one ticker
 *   GET /api/tickers/quotes           -> all cached quotes (dashboard)
 * <p>
 * Intentionally serves cached data only — does not proxy to Finnhub on demand.
 * This protects our API quota and keeps the dashboard fast.
 */
@RestController
@RequestMapping("/api/tickers")
@RequiredArgsConstructor
public class TickerController {

    private final AppTickerConfig tickerConfig;
    private final PriceCacheRepository priceCacheRepository;

    @GetMapping
    public List<String> listAllowedTickers() {
        return tickerConfig.getAllowedTickers();
    }

    @GetMapping("/quotes")
    public List<PriceQuoteResponse> allQuotes() {
        return priceCacheRepository
                .findByTickerIn(tickerConfig.getAllowedTickers()).stream()
                .map(PriceQuoteResponse::from)
                .toList();
    }

    @GetMapping("/{symbol}/quote")
    public PriceQuoteResponse quote(@PathVariable String symbol) {
        String ticker = symbol.toUpperCase();
        if (!tickerConfig.getAllowedTickers().contains(ticker)) {
            throw new InvalidTickerException("Ticker " + ticker + " is not supported");
        }
        return priceCacheRepository.findById(ticker)
                .map(PriceQuoteResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No cached quote for " + ticker + " yet — poller may not have run."));
    }
}