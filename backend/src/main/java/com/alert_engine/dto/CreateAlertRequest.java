package com.alert_engine.dto;

import com.alert_engine.model.enums.AlertDirection;
import com.alert_engine.model.enums.NotificationChannel;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Payload for POST /api/alerts.
 * <p>
 * Validation annotations here are the first line of defense — they run before
 * the controller method body via @Valid. Anything they reject never reaches the
 * service layer.
 */
public record CreateAlertRequest(
        @NotBlank(message = "Ticker is required")
        @Pattern(regexp = "^[A-Z]{1,10}$", message = "Ticker must be 1-10 uppercase letters")
        String ticker,

        @NotNull(message = "Threshold price is required")
        @DecimalMin(value = "0.0001", message = "Threshold must be positive")
        @Digits(integer = 14, fraction = 4, message = "Threshold must fit NUMERIC(18,4)")
        BigDecimal thresholdPrice,

        @NotNull(message = "Direction is required")
        AlertDirection direction,

        @NotNull(message = "Cooldown is required")
        @Min(value = 0, message = "Cooldown must be non-negative")
        @Max(value = 86400, message = "Cooldown must be at most 24h (86400s)")
        Integer cooldownSeconds,

        @NotEmpty(message = "At least one channel is required")
        Set<NotificationChannel> channels
) {}