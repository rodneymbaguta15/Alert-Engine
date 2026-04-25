package com.alert_engine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Enables STOMP-over-WebSocket. Clients connect to /ws, subscribe to
 * /topic/alerts/user/{userId} for per-user pushes.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Single endpoint registration with both SockJS fallback AND origin patterns
        // applied to it. This allows WebSocket connections from localhost and falls back to SockJS if WebSockets aren't supported.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:*", "https://localhost:*")
                .withSockJS();
    }
}