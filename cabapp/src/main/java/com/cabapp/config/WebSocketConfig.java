package com.cabapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket Configuration for Real-Time Communications
 * 
 * Endpoints:
 * - /ws: Main WebSocket connection endpoint for React frontend
 * 
 * Channels:
 * - /topic/drivers: Broadcast all driver locations to all users
 * - /topic/ride/{rideId}: Ride-specific updates (status, ETA, etc.)
 * - /user/{userId}/queue/notifications: Personal notifications
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configure message broker for different types of real-time updates
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable simple in-memory message broker for subscriptions
        config.enableSimpleBroker(
            "/topic",  // Public channels (all users can subscribe)
            "/queue"   // Private channels (user-specific)
        );
        
        // Application destination prefix for client messages
        config.setApplicationDestinationPrefixes("/app");
        
        // User destination prefix for personal messages
        config.setUserDestinationPrefix("/user");
    }

    /**
     * Register WebSocket endpoints for client connections
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Main WebSocket endpoint with SockJS fallback
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")  // Allow React frontend (localhost:3000)
                .withSockJS()  // Fallback for browsers that don't support WebSocket
                .setHeartbeatTime(25000);  // Keep connection alive
        
        // Direct WebSocket endpoint (without SockJS)
        registry.addEndpoint("/ws-direct")
                .setAllowedOriginPatterns("*");
    }
}