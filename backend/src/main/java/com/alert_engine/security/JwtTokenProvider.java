package com.alert_engine.security;

import com.alert_engine.config.JwtConfig;
import com.alert_engine.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Utility for issuing and parsing JWT access tokens. Uses HS256 symmetric signing.
 *
 * The secret key is configured via JwtConfig, which should be populated from
 * application.properties or environment variables. It must be at least 32 bytes
 * (256 bits) for HS256; this is checked at startup.
 *
 * Issued tokens include the user's email as the subject and their user ID as a
 * custom claim. They also include standard claims like issuer, issuedAt, and
 * expiration.
 *
 * Parsing a token verifies the signature and checks standard claims like issuer
 * and expiration. If anything is wrong (bad signature, expired, malformed),
 * parse() throws a JwtException.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            // Fail fast — a too-short secret would let jjwt throw at first use.
            throw new IllegalStateException(
                    "jwt.secret must be at least 32 bytes (got " + keyBytes.length + ")");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JwtTokenProvider initialized (issuer={}, expiration={})",
                jwtConfig.getIssuer(), jwtConfig.getExpiration());
    }

    /** Issue a new token for the given user. */
    public String issueToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(jwtConfig.getExpiration());

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .issuer(jwtConfig.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parse + verify a token. Returns the parsed claims, or throws if anything
     * is wrong (bad signature, expired, malformed, etc.).
     */
    public Claims parse(String token) throws JwtException {
        Jws<Claims> parsed = Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(jwtConfig.getIssuer())
                .build()
                .parseSignedClaims(token);
        return parsed.getPayload();
    }
}