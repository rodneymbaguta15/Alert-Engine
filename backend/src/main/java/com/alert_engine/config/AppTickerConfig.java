package com.alert_engine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * Application-level polling/ticker config.
 * <p>
 * The "allowed tickers" list is the symbols a trader is permitted
 * to create alerts for. Starting with 3; will be replaced by a
 * dynamic "supported symbols" lookup later.
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppTickerConfig {

    /** Tickers the app is willing to poll and alert on. Validated in AlertConfigService. */
    private List<String> allowedTickers = List.of("AAPL", "NVDA", "GOOGL");

    /** How often the poller runs. 60s to stay under Finnhub free tier (60 calls/min). */
    private Duration pollingInterval = Duration.ofSeconds(60);
}