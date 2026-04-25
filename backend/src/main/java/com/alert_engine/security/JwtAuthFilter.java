package com.alert_engine.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Per-request filter that reads "Authorization: Bearer <jwt>", validates the
 * token, and populates SecurityContext with an Authentication carrying our
 * AppPrincipal.
 *
 * No header / invalid token => no auth set => SecurityConfig's filter chain
 * decides whether the endpoint is public (auth optional) or protected (401).
 *
 * OncePerRequestFilter ensures we don't re-process forwarded requests.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String token = extractToken(request);
        if (token != null) {
            try {
                Claims claims = tokenProvider.parse(token);

                Long userId = claims.get("userId", Long.class);
                String email = claims.getSubject();
                if (userId == null || email == null) {
                    throw new JwtException("Token missing required claims");
                }

                AppPrincipal principal = new AppPrincipal(userId, email);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                );
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException e) {
                // Invalid/expired token. Log at debug — hostile probes will spam this.
                log.debug("JWT validation failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith(PREFIX)) return null;
        return header.substring(PREFIX.length()).trim();
    }
}