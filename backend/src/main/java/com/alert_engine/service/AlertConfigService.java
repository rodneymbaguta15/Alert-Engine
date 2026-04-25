package com.alert_engine.service;

import com.alert_engine.config.AppTickerConfig;
import com.alert_engine.dto.CreateAlertRequest;
import com.alert_engine.dto.UpdateAlertRequest;
import com.alert_engine.dto.AlertConfigResponse;
import com.alert_engine.exception.InvalidTickerException;
import com.alert_engine.exception.ResourceNotFoundException;
import com.alert_engine.model.AlertConfig;
import com.alert_engine.model.User;
import com.alert_engine.repository.AlertConfigRepository;
import com.alert_engine.repository.UserRepository;
import com.alert_engine.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for alert configs. Now reads userId from SecurityContext
 * via CurrentUserService instead of the old hardcoded constant.
 *
 * Every read AND mutation path is scoped by user id — Trader 1 can never see
 * or modify Trader 2's alerts even with a guessed id.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertConfigService {

    private final AlertConfigRepository alertConfigRepository;
    private final UserRepository userRepository;
    private final AppTickerConfig tickerConfig;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<AlertConfigResponse> listAll() {
        Long userId = currentUserService.currentUserId();
        return alertConfigRepository.findByUserId(userId).stream()
                .map(AlertConfigResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlertConfigResponse getById(Long id) {
        Long userId = currentUserService.currentUserId();
        AlertConfig config = alertConfigRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert config " + id + " not found"));
        return AlertConfigResponse.from(config);
    }

    @Transactional
    public AlertConfigResponse create(CreateAlertRequest request) {
        validateTicker(request.ticker());
        Long userId = currentUserService.currentUserId();

        User userRef = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user " + userId + " not found in DB"));

        AlertConfig config = AlertConfig.builder()
                .user(userRef)
                .ticker(request.ticker())
                .thresholdPrice(request.thresholdPrice())
                .direction(request.direction())
                .cooldownSeconds(request.cooldownSeconds())
                .channels(request.channels())
                .enabled(true)
                .isArmed(true)
                .build();

        AlertConfig saved = alertConfigRepository.save(config);
        log.info("User {} created alert {} for {} ({} {})",
                userId, saved.getId(), saved.getTicker(),
                saved.getDirection(), saved.getThresholdPrice());
        return AlertConfigResponse.from(saved);
    }

    @Transactional
    public AlertConfigResponse update(Long id, UpdateAlertRequest request) {
        Long userId = currentUserService.currentUserId();
        AlertConfig config = alertConfigRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert config " + id + " not found"));

        boolean structuralChange =
                !config.getThresholdPrice().equals(request.thresholdPrice()) ||
                        config.getDirection() != request.direction();

        config.setThresholdPrice(request.thresholdPrice());
        config.setDirection(request.direction());
        config.setCooldownSeconds(request.cooldownSeconds());
        config.setChannels(request.channels());
        config.setEnabled(request.enabled());
        if (structuralChange) {
            config.setIsArmed(true);
            log.info("Re-armed config {} (user {}) due to threshold/direction change", id, userId);
        }

        AlertConfig saved = alertConfigRepository.save(config);
        return AlertConfigResponse.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Long userId = currentUserService.currentUserId();
        AlertConfig config = alertConfigRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert config " + id + " not found"));
        alertConfigRepository.delete(config);
        log.info("User {} deleted alert config {}", userId, id);
    }

    private void validateTicker(String ticker) {
        if (!tickerConfig.getAllowedTickers().contains(ticker)) {
            throw new InvalidTickerException(
                    "Ticker " + ticker + " is not supported. Allowed: " +
                            tickerConfig.getAllowedTickers());
        }
    }
}