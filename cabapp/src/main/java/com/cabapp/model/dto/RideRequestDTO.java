package com.cabapp.model.dto;

import com.cabapp.model.PaymentMethod;
import com.cabapp.model.VehicleType;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideRequestDTO {

    @NotBlank(message = "User ID is required")
    private String userId;

    // AUTO-POPULATED from user's current location (GPS)
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress; // Auto-generated from coordinates

    // AUTO-POPULATED from address geocoding
    private Double dropoffLatitude;
    private Double dropoffLongitude;

    @NotBlank(message = "Dropoff address is required")
    private String dropoffAddress; // User enters this, coordinates auto-generated

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType; // This determines pricing, not passenger count

    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CASH; // Default to CASH

    private String promoCode;
    private String notes;
    private Boolean scheduledRide;
    private LocalDateTime scheduledTime;

    // Removed passenger count - vehicle type handles capacity and pricing
    // Removed child seat, wheelchair - these are vehicle type features
    // Removed preferred driver - system auto-assigns

    // Location handling flags
    private Boolean useCurrentLocation;
    private Boolean locationPermissionGranted;
}