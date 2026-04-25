package com.alert_engine.service.price;

import com.alert_engine.config.AppTickerConfig;
import com.alert_engine.exception.FinnhubApiException;
import com.alert_engine.model.PriceCache;
import com.alert_engine.repository.PriceCacheRepository;
import com.alert_engine.service.evaluation.AlertEvaluatorService;
import com.alert_engine.service.evaluation.EvaluationResult;
import com.alert_engine.service.notification.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Scheduled poller: fetch quotes → upsert price_cache → evaluate alerts → dispatch.
 * Dispatch is async, so the scheduler returns quickly even if email is slow.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PricePollingService {

    private final FinnhubClient finnhubClient;
    private final PriceCacheRepository priceCacheRepository;
    private final AppTickerConfig tickerConfig;
    private final AlertEvaluatorService alertEvaluatorService;
    private final NotificationDispatcher notificationDispatcher;

    @Scheduled(
            fixedDelayString = "#{@appTickerConfig.pollingInterval.toMillis()}",
            initialDelay = 10_000   // delay startup by 10s to allow app to initialize and avoid thundering herd on restart
    )
    @Transactional
    public void pollPrices() {
        List<String> tickers = tickerConfig.getAllowedTickers();
        log.info("Polling prices for {} ticker(s): {}", tickers.size(), tickers);

        int successes = 0;
        int failures = 0;
        int triggered = 0;
        int suppressed = 0;

        for (String ticker : tickers) {
            try {
                FinnhubQuoteResponse quote = finnhubClient.getQuote(ticker);
                upsertPriceCache(ticker, quote);
                successes++;

                List<EvaluationResult> results =
                        alertEvaluatorService.evaluateForTicker(ticker, quote.getCurrentPrice());

                for (EvaluationResult result : results) {
                    switch (result.outcome()) {
                        case TRIGGERED -> {
                            triggered++;
                            notificationDispatcher.dispatch(result);   // async
                        }
                        case SUPPRESSED_COOLDOWN -> {
                            suppressed++;
                            notificationDispatcher.dispatch(result);   // async; records history only
                        }
                        case NOT_TRIGGERED -> {
                            // silent; most common outcome
                        }
                    }
                }
            } catch (FinnhubApiException e) {
                log.warn("Failed to fetch quote for {}: {}", ticker, e.getMessage());
                failures++;
            } catch (Exception e) {
                log.error("Unexpected error processing {}", ticker, e);
                failures++;
            }
        }

        log.info("Poll complete: {} succeeded, {} failed, {} triggered, {} suppressed",
                successes, failures, triggered, suppressed);
    }

    private void upsertPriceCache(String ticker, FinnhubQuoteResponse quote) {
        PriceCache cache = priceCacheRepository.findById(ticker)
                .orElseGet(() -> {
                    PriceCache newEntry = new PriceCache();
                    newEntry.setTicker(ticker);
                    return newEntry;
                });

        cache.setCurrentPrice(quote.getCurrentPrice());
        cache.setPreviousClose(quote.getPreviousClose());
        cache.setHighPrice(quote.getHighPrice());
        cache.setLowPrice(quote.getLowPrice());
        cache.setOpenPrice(quote.getOpenPrice());
        cache.setQuoteTimestamp(
                quote.getTimestamp() != null ? Instant.ofEpochSecond(quote.getTimestamp()) : null);
        cache.setFetchedAt(Instant.now());

        priceCacheRepository.save(cache);
        log.debug("Upserted price for {}: {}", ticker, quote.getCurrentPrice());
    }
}