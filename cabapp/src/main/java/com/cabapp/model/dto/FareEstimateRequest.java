package com.cabapp.model.dto;

import com.cabapp.model.VehicleType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateRequest {

    @NotNull(message = "Pickup latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double pickupLatitude;

    @NotNull(message = "Pickup longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double pickupLongitude;

    @NotNull(message = "Dropoff latitude is required")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double dropoffLatitude;

    @NotNull(message = "Dropoff longitude is required")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double dropoffLongitude;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    private String promoCode;

    private String userId;

    private Boolean scheduledRide;

    private String scheduledTime;
}