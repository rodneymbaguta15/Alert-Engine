package com.alert_engine.controller;

import com.alert_engine.dto.AlertHistoryResponse;
import com.alert_engine.repository.AlertHistoryRepository;
import com.alert_engine.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
/**
 * Endpoint: GET /api/history
 *
 * Returns a paginated list of the current user's alert history, ordered by
 * triggeredAt descending. Page and size query params are optional with defaults.
 *
 * This is a read-only endpoint, so no need for separate service layer logic.
 */
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class AlertHistoryController {

    private final AlertHistoryRepository alertHistoryRepository;
    private final CurrentUserService currentUserService;

    @GetMapping
    public Page<AlertHistoryResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = currentUserService.currentUserId();
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize);

        return alertHistoryRepository
                .findByUserIdOrderByTriggeredAtDesc(userId, pageable)
                .map(AlertHistoryResponse::from);
    }
}