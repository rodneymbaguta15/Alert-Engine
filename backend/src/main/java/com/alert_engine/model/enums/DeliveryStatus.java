package com.alert_engine.model.enums;

/**
 * Outcome of an alert delivery attempt.
 * SENT: successfully delivered through the channel.
 * FAILED: delivery was attempted but failed (SMTP error, WebSocket not connected, etc.).
 * SUPPRESSED_COOLDOWN: not delivered because the cooldown window had not elapsed.
 */
public enum DeliveryStatus {
    SENT,
    FAILED,
    SUPPRESSED_COOLDOWN
}
