package com.cabapp.model.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocation {

    private String driverId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private String status; // AVAILABLE, BUSY, ASSIGNED
    private Double speed; // km/h
    private Double heading; // Direction in degrees
    private Double accuracy; // GPS accuracy in meters
    private Long currentRideId;

    // Validation methods
    public boolean isValidCoordinates() {
        return latitude != null && longitude != null &&
                latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180;
    }

    public boolean isInAtlantaServiceArea() {
        if (!isValidCoordinates()) return false;

        // Atlanta service area bounds
        return latitude >= 33.4 && latitude <= 34.1 &&
                longitude >= -84.8 && longitude <= -84.0;
    }

    // Helper method to create location update
    public static DriverLocation create(String driverId, Double lat, Double lng, String status) {
        return DriverLocation.builder()
                .driverId(driverId)
                .latitude(lat)
                .longitude(lng)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Calculate distance to another location (simple approximation)
    public double distanceTo(double otherLat, double otherLng) {
        if (!isValidCoordinates()) return Double.MAX_VALUE;

        double latDiff = Math.toRadians(otherLat - this.latitude);
        double lngDiff = Math.toRadians(otherLng - this.longitude);

        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(otherLat)) *
                        Math.sin(lngDiff / 2) * Math.sin(lngDiff / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371 * c; // Distance in kilometers
    }
}