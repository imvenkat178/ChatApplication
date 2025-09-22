package com.cabapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "drivers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    private String id; // Driver ID

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(unique = true, nullable = false)
    private String licensePlate;

    private String licenseNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DriverStatus status = DriverStatus.OFFLINE;

    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    // Current location (for real-time tracking)
    private Double currentLatitude;
    private Double currentLongitude;

    @Column(name = "last_location_update")
    private LocalDateTime lastLocationUpdate;

    // Current ride assignment
    private Long currentRideId;

    // Driver profile
    private String vehicleModel;
    private String vehicleColor;
    private Integer vehicleYear;
    private Double rating;
    private Integer totalRides;

    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_status_update")
    private LocalDateTime lastStatusUpdate;

    // Helper methods
    public void updateLocation(double latitude, double longitude) {
        this.currentLatitude = latitude;
        this.currentLongitude = longitude;
        this.lastLocationUpdate = LocalDateTime.now();
    }

    public boolean hasCurrentLocation() {
        return currentLatitude != null && currentLongitude != null;
    }

    public boolean isAvailable() {
        return status == DriverStatus.AVAILABLE;
    }

    public boolean isOnRide() {
        return currentRideId != null && (status == DriverStatus.BUSY);
    }

    public String getVehicleInfo() {
        return String.format("%s %s %s (%s)",
                vehicleYear != null ? vehicleYear : "",
                vehicleColor != null ? vehicleColor : "",
                vehicleModel != null ? vehicleModel : "",
                licensePlate);
    }
}