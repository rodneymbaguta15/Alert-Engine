package com.alert_engine.service;

import com.alert_engine.dto.LoginRequest;
import com.alert_engine.dto.LoginResponse;
import com.alert_engine.dto.UserResponse;
import com.alert_engine.model.User;
import com.alert_engine.repository.UserRepository;
import com.alert_engine.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Login flow:
 *   1. Look up user by email
 *   2. Verify bcrypt password match
 *   3. Issue JWT
 *
 * No registration endpoint in this dev phase — users are seeded via Flyway.
 *
 * Bad-credentials response is intentionally vague: same error whether the email
 * doesn't exist or the password is wrong, and same response time (bcrypt always
 * runs even on missing users isn't enforced here, but it would be in prod).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.debug("Login failed: no user with email {}", request.email());
                    return new BadCredentialsException("Invalid credentials");
                });

        if (!user.getEnabled()) {
            log.debug("Login failed: user {} is disabled", request.email());
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.debug("Login failed: bad password for {}", request.email());
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = tokenProvider.issueToken(user);
        log.info("User {} (id={}) logged in", user.getEmail(), user.getId());

        return new LoginResponse(token, UserResponse.from(user));
    }
}