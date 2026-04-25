package com.alert_engine.dto;

import com.alert_engine.model.AlertHistory;
import com.alert_engine.model.enums.AlertDirection;
import com.alert_engine.model.enums.DeliveryStatus;
import com.alert_engine.model.enums.NotificationChannel;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response shape for alert history reads. This is what the frontend gets when it
 * calls GET /api/alerts/history. It includes details about each triggered alert,
 * including delivery status and any error messages.
 */
public record AlertHistoryResponse(
        Long id,
        Long alertConfigId,
        String ticker,
        BigDecimal triggeredPrice,
        BigDecimal thresholdPrice,
        AlertDirection direction,
        NotificationChannel channel,
        DeliveryStatus deliveryStatus,
        String errorMessage,
        Instant triggeredAt
) {
    public static AlertHistoryResponse from(AlertHistory history) {
        return new AlertHistoryResponse(
                history.getId(),
                history.getAlertConfig().getId(),
                history.getTicker(),
                history.getTriggeredPrice(),
                history.getThresholdPrice(),
                history.getDirection(),
                history.getChannel(),
                history.getDeliveryStatus(),
                history.getErrorMessage(),
                history.getTriggeredAt()
        );
    }
}