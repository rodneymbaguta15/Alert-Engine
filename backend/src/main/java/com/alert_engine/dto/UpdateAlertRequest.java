package com.alert_engine.dto;

import com.alert_engine.model.enums.AlertDirection;
import com.alert_engine.model.enums.NotificationChannel;
import jakarta.validation.constraints.*;

        import java.math.BigDecimal;
import java.util.Set;

/**
 * Payload for PUT /api/alerts/{id}.
 * <p>
 * All fields are required — this is a full replacement (PUT semantics).
 * <p>
 * Notably, ticker is NOT updatable — once you create an alert for AAPL,
 * you can't re-point it to NVDA. Delete and re-create instead. Keeps
 * alert_history semantics clean (history rows reference a config that
 * has always been for one ticker).
 */
public record UpdateAlertRequest(
        @NotNull(message = "Threshold price is required")
        @DecimalMin(value = "0.0001", message = "Threshold must be positive")
        @Digits(integer = 14, fraction = 4)
        BigDecimal thresholdPrice,

        @NotNull(message = "Direction is required")
        AlertDirection direction,

        @NotNull(message = "Cooldown is required")
        @Min(0) @Max(86400)
        Integer cooldownSeconds,

        @NotEmpty(message = "At least one channel is required")
        Set<NotificationChannel> channels,

        @NotNull(message = "Enabled flag is required")
        Boolean enabled
) {}