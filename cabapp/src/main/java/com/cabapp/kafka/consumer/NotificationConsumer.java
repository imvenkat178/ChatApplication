package com.cabapp.kafka.consumer;

import com.cabapp.model.dto.NotificationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "user-notifications", groupId = "notification-service")
    public void processUserNotification(String notificationJson) {
        try {
            NotificationDTO notification = objectMapper.readValue(notificationJson, NotificationDTO.class);

            if ("BROADCAST".equals(notification.getUserId())) {
                // Broadcast to all users
                messagingTemplate.convertAndSend("/topic/system-status", notification);
                log.info("Broadcasted system notification: {}", notification.getTitle());
            } else {
                // Send to specific user
                String destination = determineDestination(notification);
                messagingTemplate.convertAndSendToUser(notification.getUserId(), destination, notification);

                log.debug("Sent notification to user {}: type={}",
                        notification.getUserId(), notification.getType());
            }

        } catch (Exception e) {
            log.error("Error processing user notification: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "emergency-alerts", groupId = "emergency-service")
    public void processEmergencyAlert(String alertJson) {
        try {
            NotificationDTO alert = objectMapper.readValue(alertJson, NotificationDTO.class);

            // Send to admin dashboard immediately
            messagingTemplate.convertAndSend("/topic/admin/emergency", alert);

            // Send to emergency response team
            messagingTemplate.convertAndSend("/topic/emergency-response", alert);

            // Log for audit trail
            log.error("EMERGENCY ALERT processed: User={}, Message={}, Location={}",
                    alert.getUserId(), alert.getMessage(), alert.getData());

        } catch (Exception e) {
            log.error("Error processing emergency alert: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "driver-notifications", groupId = "driver-notification-service")
    public void processDriverNotification(String notificationJson) {
        try {
            NotificationDTO notification = objectMapper.readValue(notificationJson, NotificationDTO.class);

            // Send notification to driver
            messagingTemplate.convertAndSendToUser(notification.getDriverId(),
                    "/topic/driver-notifications", notification);

            log.debug("Sent notification to driver {}: type={}",
                    notification.getDriverId(), notification.getType());

        } catch (Exception e) {
            log.error("Error processing driver notification: {}", e.getMessage(), e);
        }
    }

    // Determine WebSocket destination based on notification type
    private String determineDestination(NotificationDTO notification) {
        switch (notification.getType()) {
            case NotificationDTO.DRIVER_LOCATION_UPDATE:
                return "/topic/driver-location";
            case NotificationDTO.EMERGENCY:
                return "/topic/emergency";
            case NotificationDTO.SYSTEM_STATUS:
                return "/topic/system-status";
            default:
                return "/topic/notifications";
        }
    }
}