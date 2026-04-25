package com.alert_engine.dto;

import com.alert_engine.model.User;

/**
 * Public-facing user info. Never expose passwordHash.
 */
public record UserResponse(
        Long id,
        String email,
        String displayName
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName());
    }
}