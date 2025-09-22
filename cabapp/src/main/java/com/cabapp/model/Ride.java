package com.cabapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    private String driverId; // Assigned when driver accepts

    // Pickup location
    @Column(nullable = false)
    private Double pickupLatitude;

    @Column(nullable = false)
    private Double pickupLongitude;

    private String pickupAddress;

    // Dropoff location
    @Column(nullable = false)
    private Double dropoffLatitude;

    @Column(nullable = false)
    private Double dropoffLongitude;

    @Column(nullable = false)
    private String dropoffAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RideStatus status = RideStatus.REQUESTED;

    private Double estimatedFare;
    private Double actualFare; // Final fare after ride completion
    private Double distance;
    private String promoCode;
    private String notes;

    // Future ride scheduling (optional)
    private Boolean scheduledRide;
    private LocalDateTime scheduledTime;

    // AUTOMATIC TIMESTAMPS - System generated
    @CreationTimestamp
    @Column(name = "ride_requested_at", nullable = false)
    private LocalDateTime rideRequestedAt; // When user books ride

    @Column(name = "ride_accepted_at")
    private LocalDateTime rideAcceptedAt; // When driver accepts

    @Column(name = "driver_arrived_at")
    private LocalDateTime driverArrivedAt; // When driver reaches pickup

    @Column(name = "ride_started_at")
    private LocalDateTime rideStartedAt; // When ride begins

    @Column(name = "ride_completed_at")
    private LocalDateTime rideCompletedAt; // When ride ends

    @Column(name = "ride_cancelled_at")
    private LocalDateTime rideCancelledAt; // If ride is cancelled

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Helper methods to set timestamps automatically
    public void acceptRide(String driverId) {
        this.driverId = driverId;
        this.status = RideStatus.ASSIGNED;
        this.rideAcceptedAt = LocalDateTime.now();
    }

    public void markDriverArrived() {
        this.status = RideStatus.DRIVER_ARRIVED;
        this.driverArrivedAt = LocalDateTime.now();
    }

    public void startRide() {
        this.status = RideStatus.IN_PROGRESS;
        this.rideStartedAt = LocalDateTime.now();
    }

    public void completeRide(Double actualFare) {
        this.status = RideStatus.COMPLETED;
        this.actualFare = actualFare;
        this.rideCompletedAt = LocalDateTime.now();
    }

    public void cancelRide() {
        this.status = RideStatus.CANCELLED;
        this.rideCancelledAt = LocalDateTime.now();
    }
}