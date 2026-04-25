package com.alert_engine.service.price;

import com.alert_engine.config.FinnhubConfig;
import com.alert_engine.exception.FinnhubApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Thin wrapper over Finnhub's /quote endpoint. Responsibilities:
 *  - URL building with the API key
 *  - Error translation (network/HTTP errors to FinnhubApiException)
 *  - Logging
 * <p>
 * Not responsible for: caching, scheduling, or persistence — those belong in
 * PricePollingService. Keeping this class focused makes it trivial to stub in
 * unit tests.
 */
@Component
@Slf4j
public class FinnhubClient {

    private final RestTemplate restTemplate;
    private final FinnhubConfig config;

    // Constructor injection
    public FinnhubClient(RestTemplate finnhubRestTemplate, FinnhubConfig config) {
        this.restTemplate = finnhubRestTemplate;
        this.config = config;
    }

    /**
     * Fetch the latest quote for a ticker symbol.
     *
     * @param symbol stock ticker (e.g., "AAPL")
     * @return parsed quote response
     * @throws FinnhubApiException if the request fails or returns an unusable payload
     */
    public FinnhubQuoteResponse getQuote(String symbol) {
        URI uri = UriComponentsBuilder.fromUriString(config.getBaseUrl())
                .path("/quote")
                .queryParam("symbol", symbol)
                .queryParam("token", config.getApiKey())
                .build()
                .toUri();

        try {
            // We DON'T log the URI directly — it contains the API key.
            log.debug("Fetching quote for {}", symbol);

            FinnhubQuoteResponse response = restTemplate.getForObject(uri, FinnhubQuoteResponse.class);

            if (response == null) {
                throw new FinnhubApiException("Empty response body for symbol " + symbol);
            }
            if (!response.hasValidQuote()) {
                // Finnhub returns c=0 for unknown symbols. Not a network failure,
                // but not useful either. Flag it clearly for the caller.
                throw new FinnhubApiException(
                        "No valid quote returned for symbol " + symbol + " (possible invalid ticker or market closed)");
            }
            return response;

        } catch (RestClientException e) {
            // Catches connection refused, timeouts, 4xx/5xx responses, parse errors.
            throw new FinnhubApiException(
                    "Finnhub API call failed for symbol " + symbol + ": " + e.getMessage(), e);
        }
    }
}