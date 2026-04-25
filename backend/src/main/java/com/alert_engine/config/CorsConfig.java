package com.alert_engine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the React dev server to hit the API during development.
 * <p>
 * In production, tighten to the real frontend origin via
 * a property (e.g., cors.allowed-origins from application.properties).
 * For now we accept any localhost port so Vite (5173), CRA (3000),
 * and the static WS test page (8080) all work.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("http://localhost:*", "https://localhost:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // Cache preflight response for 1 hour
    }
}