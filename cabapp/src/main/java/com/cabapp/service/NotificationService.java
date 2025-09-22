package com.cabapp.service;

import com.cabapp.model.Ride;
import com.cabapp.model.dto.NotificationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Notify user that ride was created successfully with auto-coordinates
    public void notifyRideCreated(String userId, Ride ride) {
        NotificationDTO notification = NotificationDTO.builder()
                .type("RIDE_CREATED")
                .title("Ride Booked Successfully!")
                .message(String.format("Your ride from %s to %s has been booked. Estimated fare: ₹%.2f",
                        ride.getPickupAddress(),
                        ride.getDropoffAddress(),
                        ride.getEstimatedFare()))
                .rideId(ride.getId())
                .timestamp(ride.getRideRequestedAt())
                .build();

        messagingTemplate.convertAndSendToUser(
                userId, "/topic/notifications", notification);

        log.info("Notified user {} about ride creation with auto-coordinates: {}",
                userId, ride.getId());
    }

    // Notify user about driver assignment with location info
    public void notifyDriverAssigned(String userId, String driverId, Ride ride) {
        NotificationDTO notification = NotificationDTO.builder()
                .type("DRIVER_ASSIGNED")
                .title("Driver Assigned!")
                .message(String.format("Driver is heading to your pickup location at %s",
                        ride.getPickupAddress()))
                .rideId(ride.getId())
                .driverId(driverId)
                .timestamp(ride.getRideAcceptedAt())
                .build();

        messagingTemplate.convertAndSendToUser(
                userId, "/topic/notifications", notification);

        log.info("Notified user {} about driver {} assignment for ride {}",
                userId, driverId, ride.getId());
    }

    // Notify driver about ride request with pickup location
    public void notifyDriverAboutRideRequest(String driverId, Long rideId) {
        NotificationDTO notification = NotificationDTO.builder()
                .type("RIDE_REQUEST")
                .title("New Ride Request!")
                .message("You have a new ride request nearby")
                .rideId(rideId)
                .build();

        messagingTemplate.convertAndSendToUser(
                driverId, "/topic/driver-notifications", notification);

        log.info("Notified driver {} about ride request {}", driverId, rideId);
    }

    // Notify driver about ride assignment
    public void notifyDriverAboutRideAssignment(String driverId, Long rideId) {
        NotificationDTO notification = NotificationDTO.builder()
                .type("RIDE_ASSIGNED")
                .title("Ride Assigned!")
                .message("You have been assigned to a ride. Please proceed to pickup location.")
                .rideId(rideId)
                .build();

        messagingTemplate.convertAndSendToUser(
                driverId, "/topic/driver-notifications", notification);

        log.info("Notified driver {} about ride assignment {}", driverId, rideId);
    }

    // Notify user that driver has arrived at pickup
    public void notifyDriverArrived(String userId, String driverId, Ride ride) {
        NotificationDTO notification = NotificationDTO.builder()
                .type("DRIVER_ARRIVED")
                .title("Driver Arrived!")
                .message(String.format("Your driver has arrived at %s", ride.getPickupAddress()))
                .rideId(ride.getId())
                .driverId(driverId)
                .timestamp(ride.getDriverArrivedAt())
                .build();

        messagingTemplate.convertAndSendToUser(
                userId, "/topic/notifications", notification);

        log.info("Notified user {} that driver {} arrived for ride {}",
                userId, driverId, ride.getId());
    }

    // Notify user that ride has started
    public void notifyRideStarted(String userId, String driverId, Ride ride) {
        NotificationDTO notification = NotificationDTO.builder()
                .type("RIDE_STARTED")
                .title("Ride Started!")
                .message(String.format("Your ride to %s has started", ride.getDropoffAddress()))
                .rideId(ride.getId())
                .driverId(driverId)
                .timestamp(ride.getRideStartedAt())
                .build();

        messagingTemplate.convertAndSendToUser(
                userId, "/topic/notifications", notification);

        log.info("Notified user {} that ride {} started", userId, ride.getId());
    }

    // Notify user that ride is completed
    public void notifyRideCompleted(String userId, String driverId, Ride ride) {
        NotificationDTO notification = NotificationDTO.builder()
                .type("RIDE_COMPLETED")
                .title("Ride Completed!")
                .message(String.format("You have arrived at %s. Total fare: ₹%.2f",
                        ride.getDropoffAddress(),
                        ride.getActualFare() != null ? ride.getActualFare() : ride.getEstimatedFare()))
                .rideId(ride.getId())
                .driverId(driverId)
                .timestamp(ride.getRideCompletedAt())
                .build();

        messagingTemplate.convertAndSendToUser(
                userId, "/topic/notifications", notification);

        log.info("Notified user {} that ride {} completed", userId, ride.getId());
    }

    // Notify user about ride cancellation
    public void notifyRideCancelled(String userId, Ride ride, String reason) {
        NotificationDTO notification = NotificationDTO.builder()
                .type("RIDE_CANCELLED")
                .title("Ride Cancelled")
                .message(String.format("Your ride from %s to %s has been cancelled. Reason: %s",
                        ride.getPickupAddress(),
                        ride.getDropoffAddress(),
                        reason))
                .rideId(ride.getId())
                .timestamp(ride.getRideCancelledAt())
                .build();

        messagingTemplate.convertAndSendToUser(
                userId, "/topic/notifications", notification);

        log.info("Notified user {} about ride {} cancellation", userId, ride.getId());
    }

    // Notify about location permission issues
    public void notifyLocationPermissionRequired(String userId) {
        NotificationDTO notification = NotificationDTO.builder()
                .type("LOCATION_PERMISSION_REQUIRED")
                .title("Location Access Required")
                .message("Please enable location access to automatically detect your pickup location")
                .build();

        messagingTemplate.convertAndSendToUser(
                userId, "/topic/notifications", notification);

        log.info("Notified user {} about location permission requirement", userId);
    }

    // Notify about geocoding failures
    public void notifyGeocodingError(String userId, String address) {
        NotificationDTO notification = NotificationDTO.builder()
                .type("GEOCODING_ERROR")
                .title("Address Not Found")
                .message(String.format("Could not find location for address: %s. Please try a different address.", address))
                .build();

        messagingTemplate.convertAndSendToUser(
                userId, "/topic/notifications", notification);

        log.info("Notified user {} about geocoding error for address: {}", userId, address);
    }

    // Send location update to user about driver position
    public void sendDriverLocationUpdate(String userId, String driverId, double latitude, double longitude) {
        NotificationDTO notification = NotificationDTO.builder()
                .type("DRIVER_LOCATION_UPDATE")
                .title("Driver Location Update")
                .message("Driver location updated")
                .driverId(driverId)
                .data(String.format("lat=%f,lng=%f", latitude, longitude))
                .build();

        messagingTemplate.convertAndSendToUser(
                userId, "/topic/driver-location", notification);

        log.debug("Sent driver location update to user {}: {}, {}", userId, latitude, longitude);
    }

    // Broadcast system status
    public void broadcastSystemStatus(String message) {
        NotificationDTO notification = NotificationDTO.builder()
                .type("SYSTEM_STATUS")
                .title("System Update")
                .message(message)
                .build();

        messagingTemplate.convertAndSend("/topic/system-status", notification);

        log.info("Broadcasted system status: {}", message);
    }
}