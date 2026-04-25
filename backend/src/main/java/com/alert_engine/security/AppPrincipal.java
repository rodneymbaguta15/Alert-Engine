package com.alert_engine.security;

import java.security.Principal;

/**
 * Custom principal stored in the Authentication when a request is authenticated
 * via JWT. Carries both userId (numeric, for repository scoping) and email
 * (string, for the Principal contract).
 *
 * Using a record gives us immutability and value semantics for free.
 */
public record AppPrincipal(Long userId, String email) implements Principal {
    @Override
    public String getName() {
        return email;
    }
}