package com.alert_engine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Holds Finnhub-related configuration and exposes a RestTemplate configured
 * with connect/read timeouts. The timeouts matter because a stuck HTTP call inside
 * the scheduled poller would block subsequent polls.
 */
@Configuration
@ConfigurationProperties(prefix = "finnhub")
@Getter
@Setter
public class FinnhubConfig {

    /** API key, injected from ${FINNHUB_API_KEY}. */
    private String apiKey;

    /** Base URL. */
    private String baseUrl = "https://finnhub.io/api/v1";

    /** How long we wait for a TCP connection before giving up. */
    private Duration connectTimeout = Duration.ofSeconds(5);

    /** How long we wait for a response body before giving up. */
    private Duration readTimeout = Duration.ofSeconds(10);

    @Bean
    public RestTemplate finnhubRestTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(connectTimeout)
                .readTimeout(readTimeout)
                .build();
    }
}