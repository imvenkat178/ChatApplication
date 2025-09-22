package com.cabapp.model.dto;

import com.cabapp.model.RideStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideStatusUpdateRequest {

    @NotBlank(message = "Ride ID is required")
    private String rideId;

    @NotBlank(message = "Driver ID is required")
    private String driverId;

    @NotNull(message = "Ride status is required")
    private RideStatus status;

    private Double currentLatitude;

    private Double currentLongitude;

    private String notes;

    private String estimatedArrivalTime;

    private String cancellationReason;

    private String otp; // For ride verification

    private Long timestamp;
}