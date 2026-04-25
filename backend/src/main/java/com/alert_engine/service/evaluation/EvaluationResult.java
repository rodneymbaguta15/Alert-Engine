package com.alert_engine.service.evaluation;

import com.alert_engine.model.AlertConfig;

import java.math.BigDecimal;

/**
 * Result of evaluating a single AlertConfig against a current price.
 * <p>
 * Three outcomes:
 *  - NOT_TRIGGERED: price hasn't crossed the threshold, or config is disarmed
 *    waiting for price to return to the opposite side.
 *  - TRIGGERED: threshold crossed AND config was armed. Should fire and record.
 *  - SUPPRESSED_COOLDOWN: would have triggered, but cooldown hasn't elapsed.
 *    Still recorded in history (with that status) for observability.
 * <p>
 * Using a record for immutability — these objects are passed once from evaluator
 * to dispatcher and never modified.
 */
public record EvaluationResult(
        Outcome outcome,
        AlertConfig config,
        BigDecimal currentPrice,
        String reason   // human-readable; useful for logs and debugging
) {
    public enum Outcome {
        NOT_TRIGGERED,
        TRIGGERED,
        SUPPRESSED_COOLDOWN
    }

    public static EvaluationResult notTriggered(AlertConfig config, BigDecimal price, String reason) {
        return new EvaluationResult(Outcome.NOT_TRIGGERED, config, price, reason);
    }

    public static EvaluationResult triggered(AlertConfig config, BigDecimal price) {
        return new EvaluationResult(Outcome.TRIGGERED, config, price, "Threshold crossed");
    }

    public static EvaluationResult suppressed(AlertConfig config, BigDecimal price, String reason) {
        return new EvaluationResult(Outcome.SUPPRESSED_COOLDOWN, config, price, reason);
    }
}