package com.alert_engine.dto;

import com.alert_engine.model.AlertConfig;
import com.alert_engine.model.enums.AlertDirection;
import com.alert_engine.model.enums.NotificationChannel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

/**
 * Response shape for alert config reads. Deliberately does NOT include user_id —
 * the frontend always operates as the authenticated user (Phase 7-JWT). Also hides
 * is_armed, which is internal mechanics.
 */
public record AlertConfigResponse(
        Long id,
        String ticker,
        BigDecimal thresholdPrice,
        AlertDirection direction,
        Integer cooldownSeconds,
        Set<NotificationChannel> channels,
        Boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
    public static AlertConfigResponse from(AlertConfig config) {
        return new AlertConfigResponse(
                config.getId(),
                config.getTicker(),
                config.getThresholdPrice(),
                config.getDirection(),
                config.getCooldownSeconds(),
                config.getChannels(),
                config.getEnabled(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
}