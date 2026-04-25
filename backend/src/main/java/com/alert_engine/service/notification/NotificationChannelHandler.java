package com.alert_engine.service.notification;

import com.alert_engine.model.enums.NotificationChannel;

/**
 * Contract for every notification channel. Implementations are @Component beans
 * and self-identify via getChannelType() so the dispatcher can look them up by enum.
 * <p>
 * send() returns a ChannelResult rather than throwing because the dispatcher needs
 * to record per-channel outcomes in alert_history. Exceptions inside send() should
 * be caught by the implementation and converted to a failure result.
 */
public interface NotificationChannelHandler {

    /** Which channel enum this handler serves. Used by the dispatcher to route. */
    NotificationChannel getChannelType();

    /**
     * Deliver the alert. MUST NOT throw for recoverable failures (SMTP down, WS not
     * connected, etc.) — return a failure result with an errorMessage instead.
     * Throwing here risks killing the dispatcher loop.
     */
    ChannelResult send(AlertContext context);

    /**
     * Result of a single channel's delivery attempt.
     * <p>
     * Factory methods are named {@code ok()} / {@code fail()} (not {@code success()} /
     * {@code failure()}) because Java records reserve method names matching component
     * names for the auto-generated accessors — a static {@code success()} would collide
     * with the accessor for the {@code success} component.
     */
    record ChannelResult(boolean success, String errorMessage) {
        public static ChannelResult ok() {
            return new ChannelResult(true, null);
        }
        public static ChannelResult fail(String reason) {
            return new ChannelResult(false, reason);
        }
    }
}