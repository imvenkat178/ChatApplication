package com.cabapp.model.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideEvent {

    private Long rideId;
    private String userId;
    private String driverId;
    private String eventType;
    private LocalDateTime timestamp;
    private String pickupAddress;
    private String dropoffAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private Double dropoffLatitude;
    private Double dropoffLongitude;
    private String vehicleType;
    private String paymentMethod;
    private Double estimatedFare;
    private Double actualFare;
    private String data; // Additional data as JSON

    // Event types constants
    public static final String RIDE_CREATED = "RIDE_CREATED";
    public static final String DRIVER_ASSIGNED = "DRIVER_ASSIGNED";
    public static final String DRIVER_ARRIVED = "DRIVER_ARRIVED";
    public static final String RIDE_STARTED = "RIDE_STARTED";
    public static final String RIDE_COMPLETED = "RIDE_COMPLETED";
    public static final String RIDE_CANCELLED = "RIDE_CANCELLED";

    // Helper methods for creating specific events
    public static RideEvent rideCreated(Long rideId, String userId, String pickupAddress, String dropoffAddress,
                                        Double pickupLat, Double pickupLng, Double dropoffLat, Double dropoffLng,
                                        String vehicleType, Double estimatedFare) {
        return RideEvent.builder()
                .rideId(rideId)
                .userId(userId)
                .eventType(RIDE_CREATED)
                .pickupAddress(pickupAddress)
                .dropoffAddress(dropoffAddress)
                .pickupLatitude(pickupLat)
                .pickupLongitude(pickupLng)
                .dropoffLatitude(dropoffLat)
                .dropoffLongitude(dropoffLng)
                .vehicleType(vehicleType)
                .estimatedFare(estimatedFare)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RideEvent driverAssigned(Long rideId, String userId, String driverId) {
        return RideEvent.builder()
                .rideId(rideId)
                .userId(userId)
                .driverId(driverId)
                .eventType(DRIVER_ASSIGNED)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static RideEvent rideCompleted(Long rideId, String userId, String driverId, Double actualFare) {
        return RideEvent.builder()
                .rideId(rideId)
                .userId(userId)
                .driverId(driverId)
                .eventType(RIDE_COMPLETED)
                .actualFare(actualFare)
                .timestamp(LocalDateTime.now())
                .build();
    }
}