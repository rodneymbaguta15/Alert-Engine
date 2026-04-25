package com.alert_engine.service.evaluation;

import com.alert_engine.model.AlertConfig;
import com.alert_engine.model.enums.AlertDirection;
import com.alert_engine.repository.AlertConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Core alert evaluation logic.
 * <p>
 * For each enabled config, determines whether the current price crosses the
 * threshold in the configured direction, and if so, whether the alert should
 * trigger or be suppressed due to cooldown. Also handles the armed/disarmed
 * state machine to ensure alerts only fire once per crossing.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertEvaluatorService {

    private final AlertConfigRepository alertConfigRepository;
    private final CooldownService cooldownService;

    /**
     * Evaluate all enabled configs for a ticker against the given price.
     * Called by PricePollingService after each successful quote fetch.
     *
     * @return list of results (one per evaluated config). TRIGGERED results
     *         should be passed to the notification dispatcher.
     */
    @Transactional
    public List<EvaluationResult> evaluateForTicker(String ticker, BigDecimal currentPrice) {
        List<AlertConfig> configs = alertConfigRepository.findByTickerAndEnabledTrue(ticker);
        List<EvaluationResult> results = new ArrayList<>(configs.size());

        for (AlertConfig config : configs) {
            results.add(evaluateOne(config, currentPrice));
        }
        return results;
    }

    /**
     * Visible for testing and for future "evaluate on demand" use cases.
     * Package-private would be fine; leaving public for symmetry.
     */
    public EvaluationResult evaluateOne(AlertConfig config, BigDecimal currentPrice) {
        boolean crossed = isThresholdCrossed(config, currentPrice);

        // ----- State machine handling -----

        if (!crossed) {
            // Price is on the "safe" side of the threshold.
            // If we were previously disarmed (fired earlier), re-arm now — the next
            // crossing should fire again.
            if (Boolean.FALSE.equals(config.getIsArmed())) {
                log.info("Re-arming config {} ({}): price {} returned to opposite side of threshold {}",
                        config.getId(), config.getTicker(), currentPrice, config.getThresholdPrice());
                config.setIsArmed(true);
                // JPA dirty check will flush this update.
            }
            return EvaluationResult.notTriggered(config, currentPrice, "Threshold not crossed");
        }

        // Price IS on the trigger side of the threshold.
        if (Boolean.FALSE.equals(config.getIsArmed())) {
            // Already fired once for this crossing; wait for price to move back.
            return EvaluationResult.notTriggered(config, currentPrice, "Already fired for this crossing");
        }

        // Armed AND crossed → would trigger. Check cooldown as the final gate.
        if (cooldownService.isInCooldown(config)) {
            // Note: we do NOT disarm here. The cooldown is a rate limit, not a
            // crossing-state change. When cooldown expires and the next poll still
            // sees the crossing, we'll fire normally.
            return EvaluationResult.suppressed(config, currentPrice, "Within cooldown window");
        }

        // FIRE. Disarm so we don't fire again until price returns to the other side.
        config.setIsArmed(false);
        log.info("Triggering config {} ({}): {} crossed threshold {} (direction={})",
                config.getId(), config.getTicker(), currentPrice, config.getThresholdPrice(),
                config.getDirection());
        return EvaluationResult.triggered(config, currentPrice);
    }

   // ----- Private helpers -----

    private boolean isThresholdCrossed(AlertConfig config, BigDecimal currentPrice) {
        int cmp = currentPrice.compareTo(config.getThresholdPrice());
        return switch (config.getDirection()) {
            case ABOVE -> cmp >= 0;
            case BELOW -> cmp <= 0;
        };
    }
}