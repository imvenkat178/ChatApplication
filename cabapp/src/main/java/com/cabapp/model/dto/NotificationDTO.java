package com.cabapp.model.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private String type; // RIDE_CREATED, DRIVER_ASSIGNED, DRIVER_ARRIVED, etc.
    private String title;
    private String message;
    private Long rideId;
    private String userId;
    private String driverId;
    private LocalDateTime timestamp;
    private String data; // Additional data as JSON string

    // Notification types constants
    public static final String RIDE_CREATED = "RIDE_CREATED";
    public static final String DRIVER_ASSIGNED = "DRIVER_ASSIGNED";
    public static final String DRIVER_ARRIVED = "DRIVER_ARRIVED";
    public static final String RIDE_STARTED = "RIDE_STARTED";
    public static final String RIDE_COMPLETED = "RIDE_COMPLETED";
    public static final String RIDE_CANCELLED = "RIDE_CANCELLED";
    public static final String LOCATION_PERMISSION_REQUIRED = "LOCATION_PERMISSION_REQUIRED";
    public static final String GEOCODING_ERROR = "GEOCODING_ERROR";
    public static final String DRIVER_LOCATION_UPDATE = "DRIVER_LOCATION_UPDATE";
    public static final String SYSTEM_STATUS = "SYSTEM_STATUS";
    public static final String EMERGENCY = "EMERGENCY";

    // Helper methods for specific notification types
    public static NotificationDTO rideCreated(String userId, Long rideId, String pickupAddress, String dropoffAddress, Double fare) {
        return NotificationDTO.builder()
                .type(RIDE_CREATED)
                .title("Ride Booked Successfully!")
                .message(String.format("Your ride from %s to %s has been booked. Estimated fare: ₹%.2f",
                        pickupAddress, dropoffAddress, fare))
                .userId(userId)
                .rideId(rideId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static NotificationDTO driverAssigned(String userId, String driverId, Long rideId, String pickupAddress) {
        return NotificationDTO.builder()
                .type(DRIVER_ASSIGNED)
                .title("Driver Assigned!")
                .message(String.format("Driver is heading to your pickup location at %s", pickupAddress))
                .userId(userId)
                .driverId(driverId)
                .rideId(rideId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static NotificationDTO locationPermissionRequired(String userId) {
        return NotificationDTO.builder()
                .type(LOCATION_PERMISSION_REQUIRED)
                .title("Location Access Required")
                .message("Please enable location access to automatically detect your pickup location")
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static NotificationDTO geocodingError(String userId, String address) {
        return NotificationDTO.builder()
                .type(GEOCODING_ERROR)
                .title("Address Not Found")
                .message(String.format("Could not find location for address: %s. Please try a different address.", address))
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}