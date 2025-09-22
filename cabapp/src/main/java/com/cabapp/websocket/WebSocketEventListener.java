package com.cabapp.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    // Track active WebSocket sessions
    private final ConcurrentMap<String, String> activeSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> userSessions = new ConcurrentHashMap<>();

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = headerAccessor.getUser() != null ? headerAccessor.getUser().getName() : "anonymous";

        activeSessions.put(sessionId, userId);
        userSessions.put(userId, sessionId);

        log.info("New WebSocket connection established. Session: {}, User: {}", sessionId, userId);
        log.info("Active WebSocket sessions: {}", activeSessions.size());
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = activeSessions.remove(sessionId);

        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket connection closed. Session: {}, User: {}", sessionId, userId);

            // Notify about user going offline (useful for driver tracking)
            if (userId.startsWith("driver_")) {
                notifyDriverOffline(userId);
            }
        }

        log.info("Active WebSocket sessions: {}", activeSessions.size());
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String destination = headerAccessor.getDestination();
        String userId = activeSessions.get(sessionId);

        log.debug("User {} subscribed to destination: {}", userId, destination);

        // Track subscriptions for different notification types
        if (destination != null) {
            if (destination.contains("/topic/notifications")) {
                log.info("User {} subscribed to ride notifications", userId);
            } else if (destination.contains("/topic/driver-location")) {
                log.info("User {} subscribed to driver location updates", userId);
            } else if (destination.contains("/topic/driver-notifications")) {
                log.info("Driver {} subscribed to ride requests", userId);
            }
        }
    }

    @EventListener
    public void handleWebSocketUnsubscribeListener(SessionUnsubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String subscriptionId = headerAccessor.getSubscriptionId();
        String userId = activeSessions.get(sessionId);

        log.debug("User {} unsubscribed from subscription: {}", userId, subscriptionId);
    }

    // Helper method to check if user is connected via WebSocket
    public boolean isUserConnected(String userId) {
        return userSessions.containsKey(userId);
    }

    // Get session ID for a user
    public String getSessionForUser(String userId) {
        return userSessions.get(userId);
    }

    // Get all active sessions count
    public int getActiveSessionsCount() {
        return activeSessions.size();
    }

    // Get all connected user IDs
    public java.util.Set<String> getConnectedUsers() {
        return userSessions.keySet();
    }

    // Notify when driver goes offline
    private void notifyDriverOffline(String driverId) {
        try {
            // You could notify the system that driver went offline
            // This is useful for updating driver status and location tracking
            log.info("Driver {} went offline via WebSocket disconnect", driverId);

            // Send notification to admin dashboard about driver going offline
            messagingTemplate.convertAndSend("/topic/admin/driver-status",
                    String.format("Driver %s went offline", driverId));

        } catch (Exception e) {
            log.error("Error notifying driver offline: {}", e.getMessage());
        }
    }

    // Send connection statistics to admin dashboard
    public void broadcastConnectionStats() {
        try {
            java.util.Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("activeConnections", activeSessions.size());
            stats.put("connectedUsers", userSessions.size());
            stats.put("timestamp", System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/admin/connection-stats", stats);

        } catch (Exception e) {
            log.error("Error broadcasting connection stats: {}", e.getMessage());
        }
    }
}