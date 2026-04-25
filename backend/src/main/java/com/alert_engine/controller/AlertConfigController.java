package com.alert_engine.controller;

import com.alert_engine.dto.CreateAlertRequest;
import com.alert_engine.dto.UpdateAlertRequest;
import com.alert_engine.dto.AlertConfigResponse;
import com.alert_engine.service.AlertConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * CRUD for alert configs.
 * <p>
 * Endpoints:
 *   GET    /api/alerts       -> list all alerts for the current user
 *   GET    /api/alerts/{id}  -> get one alert
 *   POST   /api/alerts       -> create a new alert (201 with Location header)
 *   PUT    /api/alerts/{id}  -> update an alert (full replacement)
 *   DELETE /api/alerts/{id}  -> delete an alert (204)
 */
@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertConfigController {

    private final AlertConfigService alertConfigService;

    @GetMapping
    public List<AlertConfigResponse> list() {
        return alertConfigService.listAll();
    }

    @GetMapping("/{id}")
    public AlertConfigResponse get(@PathVariable Long id) {
        return alertConfigService.getById(id);
    }

    @PostMapping
    public ResponseEntity<AlertConfigResponse> create(@Valid @RequestBody CreateAlertRequest request) {
        AlertConfigResponse created = alertConfigService.create(request);
        // REST convention: 201 Created + Location header pointing at the new resource.
        URI location = URI.create("/api/alerts/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public AlertConfigResponse update(@PathVariable Long id,
                                      @Valid @RequestBody UpdateAlertRequest request) {
        return alertConfigService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        alertConfigService.delete(id);
    }
}