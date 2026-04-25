package com.alert_engine.service.notification;

import com.alert_engine.model.AlertConfig;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Immutable snapshot of everything a channel needs to deliver an alert.
 * <p>
 * Built by the dispatcher from an EvaluationResult + the triggering config.
 * Channels MUST NOT mutate this but treat it as a value object flowing one-way
 * from evaluator to channel.
 * <p>
 * Denormalized to avoid channels needing to navigate lazy JPA associations
 * outside of a transaction (email channel runs @Async on a different thread).
 */
public record AlertContext(
        Long configId,
        Long userId,
        String userEmail,
        String userDisplayName,
        String ticker,
        BigDecimal currentPrice,
        BigDecimal thresholdPrice,
        String direction,        // "ABOVE" or "BELOW" as string — channels don't need the enum
        Instant triggeredAt
) {
    /** Convenience factory — extracts everything from the config/user graph while a session is open. */
    public static AlertContext from(AlertConfig config, BigDecimal currentPrice) {
        return new AlertContext(
                config.getId(),
                config.getUser().getId(),
                config.getUser().getEmail(),
                config.getUser().getDisplayName(),
                config.getTicker(),
                currentPrice,
                config.getThresholdPrice(),
                config.getDirection().name(),
                Instant.now()
        );
    }
}