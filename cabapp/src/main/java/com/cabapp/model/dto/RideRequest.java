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
public class RideRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    // Pickup location - auto-populated from GPS
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String pickupAddress;

    // Dropoff location - auto-populated from address
    private Double dropoffLatitude;
    private Double dropoffLongitude;

    @NotBlank(message = "Dropoff address is required")
    private String dropoffAddress;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @Builder.Default
    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    private String promoCode;
    private String notes;
    private Boolean scheduledRide;
    private LocalDateTime scheduledTime;

    // Auto-coordinate flags
    private Boolean useCurrentLocation;
    private Boolean locationPermissionGranted;

    // Validation methods
    public boolean hasPickupCoordinates() {
        return pickupLatitude != null && pickupLongitude != null;
    }

    public boolean hasDropoffCoordinates() {
        return dropoffLatitude != null && dropoffLongitude != null;
    }

    public boolean isValidPickupCoordinates() {
        if (!hasPickupCoordinates()) return false;
        return pickupLatitude >= -90 && pickupLatitude <= 90 &&
                pickupLongitude >= -180 && pickupLongitude <= 180;
    }

    public boolean isValidDropoffCoordinates() {
        if (!hasDropoffCoordinates()) return false;
        return dropoffLatitude >= -90 && dropoffLatitude <= 90 &&
                dropoffLongitude >= -180 && dropoffLongitude <= 180;
    }

    public boolean isInAtlantaServiceArea() {
        if (!isValidPickupCoordinates()) return false;
        // Check if pickup is in Atlanta service area
        return pickupLatitude >= 33.4 && pickupLatitude <= 34.1 &&
                pickupLongitude >= -84.8 && pickupLongitude <= -84.0;
    }
}