package com.alert_engine.dto;

public record LoginResponse(
        String token,
        UserResponse user
) {}