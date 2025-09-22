package com.cabapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
    private String phoneNumber;

    // Location fields with defaults
    private String city = "Atlanta";
    private String state = "Georgia";
    private String country = "USA";

    // Permission and preference fields with defaults
    private Boolean locationPermissionGranted = false;
    private String preferredPaymentMethod = "CASH";
    private String preferredDeviceType = "MOBILE";
    private Boolean notificationsEnabled = true;

    /**
     * Set default values for optional fields
     */
    public void setDefaultValues() {
        if (city == null || city.trim().isEmpty()) {
            city = "Atlanta";
        }
        if (state == null || state.trim().isEmpty()) {
            state = "Georgia";
        }
        if (country == null || country.trim().isEmpty()) {
            country = "USA";
        }
        if (locationPermissionGranted == null) {
            locationPermissionGranted = false;
        }
        if (preferredPaymentMethod == null || preferredPaymentMethod.trim().isEmpty()) {
            preferredPaymentMethod = "CASH";
        }
        if (preferredDeviceType == null || preferredDeviceType.trim().isEmpty()) {
            preferredDeviceType = "MOBILE";
        }
        if (notificationsEnabled == null) {
            notificationsEnabled = true;
        }
    }

    // Getters for all fields
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public Boolean getLocationPermissionGranted() {
        return locationPermissionGranted;
    }

    public String getPreferredPaymentMethod() {
        return preferredPaymentMethod;
    }

    public String getPreferredDeviceType() {
        return preferredDeviceType;
    }

    public Boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }
}