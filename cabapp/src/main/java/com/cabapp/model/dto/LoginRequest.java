package com.cabapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    // Optional location data for automatic pickup detection
    private Double latitude;
    private Double longitude;
    private Boolean locationPermissionGranted;

    // Atlanta service area bounds (from your previous chat)
    private static final double ATLANTA_NORTH = 34.1;
    private static final double ATLANTA_SOUTH = 33.4;
    private static final double ATLANTA_EAST = -84.0;
    private static final double ATLANTA_WEST = -84.8;

    /**
     * Check if location data is provided
     */
    public boolean hasLocationData() {
        return latitude != null && longitude != null;
    }

    /**
     * Validate location coordinates
     */
    public boolean isValidLocationData() {
        if (!hasLocationData()) {
            return false;
        }

        // Basic coordinate validation
        return latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180;
    }

    /**
     * Check if coordinates are within Atlanta service area
     */
    public boolean isInAtlantaArea() {
        if (!isValidLocationData()) {
            return false;
        }

        return latitude >= ATLANTA_SOUTH && latitude <= ATLANTA_NORTH &&
                longitude >= ATLANTA_WEST && longitude <= ATLANTA_EAST;
    }

    // Getters for the location permission
    public Boolean getLocationPermissionGranted() {
        return locationPermissionGranted;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
}