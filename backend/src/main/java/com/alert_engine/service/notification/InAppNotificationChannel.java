package com.alert_engine.service.notification;

import com.alert_engine.model.enums.NotificationChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Pushes alerts over STOMP to the user's dashboard.
 * <p>
 * Publishes to /topic/alerts/user/{userId}. The frontend subscribes to that topic
 * and shows a toast/notification when a message arrives. Broadcasting to
 * /topic/alerts (global) is avoided — each user should only see their own alerts.
 * <p>
 * Important limitation: if the user has no active WebSocket connection, this send
 * succeeds (STOMP doesn't confirm delivery) but the message is effectively lost.
 * That's OK here — the email channel is the reliable path, in-app is best-effort
 * for active sessions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationChannel implements NotificationChannelHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public NotificationChannel getChannelType() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public ChannelResult send(AlertContext context) {
        try {
            String destination = "/topic/alerts/user/" + context.userId();

            Map<String, Object> payload = Map.of(
                    "configId", context.configId(),
                    "ticker", context.ticker(),
                    "currentPrice", context.currentPrice(),
                    "thresholdPrice", context.thresholdPrice(),
                    "direction", context.direction(),
                    "triggeredAt", context.triggeredAt().toString()
            );

            messagingTemplate.convertAndSend(destination, payload);
            log.info("In-app alert pushed to {} for config {}", destination, context.configId());
            return ChannelResult.ok();

        } catch (MessagingException e) {
            log.warn("In-app send failed for config {}: {}", context.configId(), e.getMessage());
            return ChannelResult.fail("In-app error: " + e.getMessage());
        }
    }
}