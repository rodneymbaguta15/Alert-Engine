package com.alert_engine.config;

import com.alert_engine.security.AuthenticationEntryPointImpl;
import com.alert_engine.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration:
 *   - Stateless (JWT-based, no server-side sessions)
 *   - CSRF disabled (no cookies, no traditional form posts)
 *   - JwtAuthFilter inserted before UsernamePasswordAuthenticationFilter
 *   - /api/auth/** is public; everything else requires authentication
 *   - WebSocket /ws/** allowed through; WS auth is handled separately
 *
 * The reason CorsConfig (the WebMvc one) doesn't apply to Spring Security
 * routes by default — we have to wire CORS into the security chain too.
 * Doing it here means there's only one place CORS rules need to update.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationEntryPointImpl authEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh ->
                        eh.authenticationEntryPoint(authEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints — login + actuator health checks
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health/**").permitAll()
                        // WebSocket handshake — auth happens at the STOMP CONNECT level (Phase 7b)
                        .requestMatchers("/ws/**").permitAll()
                        // The static WS test page
                        .requestMatchers("/ws-test.html").permitAll()
                        // Everything else needs a valid JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** CORS for the security chain. Mirrors what CorsConfig has for non-secured paths. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of("http://localhost:*", "https://localhost:*"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }
}