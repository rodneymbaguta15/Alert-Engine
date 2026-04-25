package com.alert_engine.service.notification;

import com.alert_engine.model.AlertConfig;
import com.alert_engine.model.AlertHistory;
import com.alert_engine.model.User;
import com.alert_engine.model.enums.AlertDirection;
import com.alert_engine.model.enums.DeliveryStatus;
import com.alert_engine.model.enums.NotificationChannel;
import com.alert_engine.repository.AlertHistoryRepository;
import com.alert_engine.service.evaluation.EvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fans out a triggered (or suppressed) alert across its configured channels,
 * catching per-channel failures independently and writing one alert_history row
 * per channel-attempt.
 * <p>
 * @Async on dispatch() so email sends don't block the scheduler thread. Return
 * type is void — fire-and-forget.
 */
@Service
@Slf4j
public class NotificationDispatcher {

    private final AlertHistoryRepository historyRepository;
    /** Lookup table: enum → handler. Populated once at startup from injected list. */
    private final Map<NotificationChannel, NotificationChannelHandler> handlersByType;

    /**
     * Spring injects the list of all NotificationChannelHandler beans. We index
     * them by getChannelType() so dispatch is an O(1) lookup per channel.
     * Failing fast if two beans claim the same channel type.
     */
    public NotificationDispatcher(AlertHistoryRepository historyRepository,
                                  List<NotificationChannelHandler> handlers) {
        this.historyRepository = historyRepository;
        this.handlersByType = new EnumMap<>(NotificationChannel.class);
        for (NotificationChannelHandler handler : handlers) {
            NotificationChannel type = handler.getChannelType();
            if (handlersByType.put(type, handler) != null) {
                throw new IllegalStateException(
                        "Multiple handlers registered for channel " + type);
            }
        }
        log.info("NotificationDispatcher initialized with channels: {}", handlersByType.keySet());
    }

    /**
     * Entry point: given an EvaluationResult (TRIGGERED or SUPPRESSED_COOLDOWN),
     * dispatch to the appropriate channels and record the outcome in history.
     * <p>
     * NOT_TRIGGERED results should not reach this method — the caller filters.
     * <p>
     * Runs async on Spring's default task executor. Keeps the scheduler thread free.
     */
    @Async
    @Transactional
    public void dispatch(EvaluationResult result) {
        AlertConfig config = result.config();

        switch (result.outcome()) {
            case TRIGGERED -> handleTriggered(config, result);
            case SUPPRESSED_COOLDOWN -> recordSuppressed(config, result);
            case NOT_TRIGGERED -> {
                // Defensive — caller should filter these out, but don't crash if one slips through.
                log.warn("Dispatcher called with NOT_TRIGGERED for config {}, ignoring",
                        config.getId());
            }
        }
    }

    private void handleTriggered(AlertConfig config, EvaluationResult result) {
        Set<NotificationChannel> targetChannels = config.getChannels();
        if (targetChannels == null || targetChannels.isEmpty()) {
            log.warn("Config {} triggered but has no channels configured — nothing to dispatch",
                    config.getId());
            return;
        }

        AlertContext context = AlertContext.from(config, result.currentPrice());

        for (NotificationChannel channel : targetChannels) {
            NotificationChannelHandler handler = handlersByType.get(channel);
            if (handler == null) {
                log.error("No handler registered for channel {} (config {})", channel, config.getId());
                persistHistory(config, context, channel, DeliveryStatus.FAILED,
                        "No handler registered for channel");
                continue;
            }

            try {
                NotificationChannelHandler.ChannelResult channelResult = handler.send(context);
                DeliveryStatus status = channelResult.success()
                        ? DeliveryStatus.SENT
                        : DeliveryStatus.FAILED;
                persistHistory(config, context, channel, status, channelResult.errorMessage());
            } catch (Exception e) {
                // Defensive — handlers are supposed to catch internally, but if one throws
                // we still record the failure and move on to the next channel.
                log.error("Unhandled exception in channel {} for config {}", channel, config.getId(), e);
                persistHistory(config, context, channel, DeliveryStatus.FAILED,
                        "Unhandled: " + e.getMessage());
            }
        }
    }

    private void recordSuppressed(AlertConfig config, EvaluationResult result) {
        AlertContext context = AlertContext.from(config, result.currentPrice());
        // One row per configured channel — consistent with how TRIGGERED is recorded.
        // This way a query like "all SENT or SUPPRESSED history for channel X" stays clean.
        for (NotificationChannel channel : config.getChannels()) {
            persistHistory(config, context, channel, DeliveryStatus.SUPPRESSED_COOLDOWN,
                    result.reason());
        }
    }

    /** One row per channel-attempt. Denormalizes ticker/threshold/direction from config. */
    private void persistHistory(AlertConfig config,
                                AlertContext context,
                                NotificationChannel channel,
                                DeliveryStatus status,
                                String errorMessage) {
        User userRef = new User();
        userRef.setId(context.userId());

        AlertHistory history = AlertHistory.builder()
                .alertConfig(config)
                .user(userRef)
                .ticker(context.ticker())
                .triggeredPrice(context.currentPrice())
                .thresholdPrice(context.thresholdPrice())
                .direction(AlertDirection.valueOf(context.direction()))
                .channel(channel)
                .deliveryStatus(status)
                .errorMessage(errorMessage)
                .build();

        historyRepository.save(history);
    }
}