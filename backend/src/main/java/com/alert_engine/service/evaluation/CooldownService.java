package com.alert_engine.service.evaluation;

import com.alert_engine.model.AlertConfig;
import com.alert_engine.model.AlertHistory;
import com.alert_engine.model.enums.DeliveryStatus;
import com.alert_engine.repository.AlertHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Encapsulates cooldown enforcement. Given a config, answers: "has enough time
 * elapsed since the last successful delivery that we're allowed to fire again?"
 * <p>
 * Source of truth is alert_history — specifically the most recent SENT row for
 * the config. FAILED rows do NOT reset the cooldown (the user never got the alert),
 * and SUPPRESSED_COOLDOWN rows don't either (that's the whole point).
 * <p>
 * With once-per-crossing semantics, cooldown mostly guards against rapid oscillation
 * of price around the threshold boundary (e.g., $199.99 ↔ $200.01 in the same second).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CooldownService {

    private final AlertHistoryRepository alertHistoryRepository;

    /**
     * @return true if the config is within its cooldown window (should NOT fire),
     *         false if enough time has elapsed (OK to fire).
     */
    public boolean isInCooldown(AlertConfig config) {
        Optional<AlertHistory> lastSent = alertHistoryRepository
                .findLatestByConfigAndStatus(config.getId(), DeliveryStatus.SENT);

        if (lastSent.isEmpty()) {
            return false;    // never fired, definitely not in cooldown
        }

        Instant lastSentAt = lastSent.get().getTriggeredAt();
        Duration cooldown = Duration.ofSeconds(config.getCooldownSeconds());
        Instant cooldownEndsAt = lastSentAt.plus(cooldown);
        Instant now = Instant.now();

        boolean inCooldown = now.isBefore(cooldownEndsAt);
        if (inCooldown) {
            log.debug("Config {} in cooldown: last sent {}, cooldown ends {}",
                    config.getId(), lastSentAt, cooldownEndsAt);
        }
        return inCooldown;
    }
}