package com.alert_engine.security;

import com.alert_engine.model.User;
import com.alert_engine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads a user by email for Spring Security. Used during login (AuthService)
 * and indirectly by the JWT filter.
 *
 * We return Spring's User as the UserDetails, but populate Authentication
 * principal with our custom AppPrincipal in the JWT filter — that gives us
 * direct access to userId without re-querying.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                user.getEnabled(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}