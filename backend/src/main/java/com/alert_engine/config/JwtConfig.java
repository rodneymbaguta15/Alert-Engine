package com.alert_engine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * JWT configuration. Secret must be at least 32 bytes (256 bits) for HS256.
 *
 * Defaults are dev-friendly. In production:
 *   - secret comes from a real secrets manager (never in application.properties)
 *   - rotate periodically
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtConfig {

    /** HS256 signing secret. Must be >= 32 bytes. Override via JWT_SECRET env var. */
    private String secret;

    /** How long an access token is valid. */
    private Duration expiration = Duration.ofHours(24);

    /** Issuer claim — purely informational, helps debug if tokens leak between systems. */
    private String issuer = "alert-engine";
}