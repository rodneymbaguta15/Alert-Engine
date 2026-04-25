package com.alert_engine.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Thin convenience wrapper for getting the current authenticated user.
 *
 * Why a service rather than calling SecurityContextHolder directly in services?
 *   1. Centralizes the "no authenticated principal" error path.
 *   2. Easier to mock in unit tests (@MockBean CurrentUserService).
 *   3. Documents intent — `currentUserId()` reads better than 4 lines of context unwrapping.
 */
@Service
public class CurrentUserService {

    /**
     * @return the userId of the currently authenticated user.
     * @throws IllegalStateException if there is no authenticated user (which
     *         shouldn't happen if SecurityConfig is correct — protected
     *         endpoints reject unauthenticated requests upstream).
     */
    public Long currentUserId() {
        return getPrincipal().userId();
    }

    public String currentUserEmail() {
        return getPrincipal().email();
    }

    private AppPrincipal getPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof AppPrincipal p)) {
            throw new IllegalStateException("No authenticated user in SecurityContext");
        }
        return p;
    }
}