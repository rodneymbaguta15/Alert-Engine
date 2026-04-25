package com.alert_engine.controller;

import com.alert_engine.dto.LoginRequest;
import com.alert_engine.dto.LoginResponse;
import com.alert_engine.dto.UserResponse;
import com.alert_engine.model.User;
import com.alert_engine.repository.UserRepository;
import com.alert_engine.security.CurrentUserService;
import com.alert_engine.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints:
 *   POST /api/auth/login   -> exchange credentials for JWT
 *   GET  /api/auth/me      -> current authenticated user (used to verify token validity)
 *
 * No /register, /logout, or /refresh in this phase — users are seeded;
 * logout is just "delete the token client-side"; refresh comes with token rotation
 * later if we shorten the expiration.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me() {
        // Re-load fresh from DB rather than reconstructing from JWT claims —
        // catches edge cases like a user being disabled after their token was issued.
        Long userId = currentUserService.currentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "JWT references nonexistent user " + userId));
        return UserResponse.from(user);
    }
}