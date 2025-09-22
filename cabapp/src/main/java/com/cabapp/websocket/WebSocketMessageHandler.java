package com.cabapp.websocket;

import com.cabapp.model.dto.NotificationDTO;
import com.cabapp.service.DriverService;
import com.cabapp.service.LocationService;
import com.cabapp.service.RideService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@Slf4j
public class WebSocketMessageHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final DriverService driverService;
    private final LocationService locationService;
    private final RideService rideService;
    private final ObjectMapper objectMapper;

    public WebSocketMessageHandler(SimpMessagingTemplate messagingTemplate,
                                   DriverService driverService,
                                   LocationService locationService,
                                   RideService rideService,
                                   ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.driverService = driverService;
        this.locationService = locationService;
        this.rideService = rideService;
        this.objectMapper = objectMapper;
    }

    // Handle driver location updates via WebSocket
    @MessageMapping("/driver/location")
    public void handleDriverLocationUpdate(@Payload Map<String, Object> locationData,
                                           SimpMessageHeaderAccessor headerAccessor,
                                           Principal principal) {
        try {
            String driverId = principal.getName();
            Double latitude = Double.valueOf(locationData.get("latitude").toString());
            Double longitude = Double.valueOf(locationData.get("longitude").toString());

            // Validate coordinates
            if (locationService.validatePickupCoordinates(latitude, longitude)) {
                // Update driver location
                driverService.updateDriverLocation(driverId, latitude, longitude);

                log.debug("Updated driver {} location via WebSocket: {}, {}",
                        driverId, latitude, longitude);

                // Broadcast location to users who are tracking this driver
                broadcastDriverLocation(driverId, latitude, longitude);

                // Send acknowledgment back to driver
                messagingTemplate.convertAndSendToUser(driverId, "/queue/location-ack",
                        Map.of("status", "success", "timestamp", System.currentTimeMillis()));

            } else {
                log.warn("Invalid coordinates received from driver {}: {}, {}",
                        driverId, latitude, longitude);

                messagingTemplate.convertAndSendToUser(driverId, "/queue/location-error",
                        Map.of("error", "Invalid coordinates"));
            }

        } catch (Exception e) {
            log.error("Error processing driver location update: {}", e.getMessage());

            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/location-error",
                    Map.of("error", "Failed to update location"));
        }
    }

    // Handle ride status updates from drivers
    @MessageMapping("/driver/ride-status")
    public void handleRideStatusUpdate(@Payload Map<String, Object> statusData,
                                       Principal principal) {
        try {
            String driverId = principal.getName();
            Long rideId = Long.valueOf(statusData.get("rideId").toString());
            String status = statusData.get("status").toString();

            log.info("Driver {} updating ride {} status to {}", driverId, rideId, status);

            // Process status update based on status type
            switch (status.toUpperCase()) {
                case "ARRIVED":
                    rideService.markDriverArrived(rideId);
                    break;
                case "STARTED":
                    rideService.startRide(rideId);
                    break;
                case "COMPLETED":
                    Double actualFare = statusData.containsKey("actualFare") ?
                            Double.valueOf(statusData.get("actualFare").toString()) : null;
                    rideService.completeRide(rideId, actualFare);
                    break;
                default:
                    log.warn("Unknown ride status update: {}", status);
            }

            // Send acknowledgment
            messagingTemplate.convertAndSendToUser(driverId, "/queue/ride-status-ack",
                    Map.of("rideId", rideId, "status", status, "timestamp", System.currentTimeMillis()));

        } catch (Exception e) {
            log.error("Error processing ride status update: {}", e.getMessage());

            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/ride-status-error",
                    Map.of("error", "Failed to update ride status"));
        }
    }

    // Handle user location permission updates
    @MessageMapping("/user/location-permission")
    public void handleLocationPermissionUpdate(@Payload Map<String, Object> permissionData,
                                               Principal principal) {
        try {
            String userId = principal.getName();
            Boolean granted = Boolean.valueOf(permissionData.get("granted").toString());

            log.info("User {} updated location permission: {}", userId, granted);

            // You could update user preferences here
            // userService.updateLocationPermission(userId, granted);

            // Send acknowledgment
            messagingTemplate.convertAndSendToUser(userId, "/queue/permission-ack",
                    Map.of("locationPermission", granted, "message",
                            granted ? "Location access enabled for auto-pickup" : "Location access disabled"));

        } catch (Exception e) {
            log.error("Error processing location permission update: {}", e.getMessage());
        }
    }

    // Handle emergency alerts
    @MessageMapping("/emergency")
    public void handleEmergencyAlert(@Payload Map<String, Object> emergencyData,
                                     Principal principal) {
        try {
            String userId = principal.getName();
            String message = emergencyData.get("message").toString();

            log.error("EMERGENCY ALERT from user {}: {}", userId, message);

            // Broadcast emergency to admin and support
            NotificationDTO emergencyNotification = NotificationDTO.builder()
                    .type("EMERGENCY")
                    .title("Emergency Alert")
                    .message(String.format("Emergency from user %s: %s", userId, message))
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend("/topic/admin/emergency", emergencyNotification);

            // Send acknowledgment to user
            messagingTemplate.convertAndSendToUser(userId, "/queue/emergency-ack",
                    Map.of("status", "Emergency alert sent", "timestamp", System.currentTimeMillis()));

        } catch (Exception e) {
            log.error("Error processing emergency alert: {}", e.getMessage());
        }
    }

    // Broadcast driver location to interested users
    private void broadcastDriverLocation(String driverId, double latitude, double longitude) {
        try {
            Map<String, Object> locationUpdate = Map.of(
                    "driverId", driverId,
                    "latitude", latitude,
                    "longitude", longitude,
                    "timestamp", System.currentTimeMillis()
            );

            // Send to users who are tracking this specific driver
            messagingTemplate.convertAndSend("/topic/driver-location/" + driverId, locationUpdate);

            // Also send to admin dashboard for monitoring
            messagingTemplate.convertAndSend("/topic/admin/driver-locations", locationUpdate);

        } catch (Exception e) {
            log.error("Error broadcasting driver location: {}", e.getMessage());
        }
    }

    // Handle ping/pong for connection health
    @MessageMapping("/ping")
    public void handlePing(Principal principal) {
        messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/pong",
                Map.of("timestamp", System.currentTimeMillis()));
    }
}