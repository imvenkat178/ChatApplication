package com.cabapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private boolean success;
    private String message;

    // User data (only populated on successful login/registration)
    private String userId;
    private String username;
    private String name;
    private String email;
    private boolean locationPermissionEnabled;
    private String city;
    private String preferredPaymentMethod;

    /**
     * Create successful authentication response
     */
    public static AuthResponse success(String userId, String username, String name,
                                       String email, boolean locationPermissionEnabled,
                                       String city, String preferredPaymentMethod) {
        AuthResponse response = new AuthResponse();
        response.success = true;
        response.message = "Authentication successful";
        response.userId = userId;
        response.username = username;
        response.name = name;
        response.email = email;
        response.locationPermissionEnabled = locationPermissionEnabled;
        response.city = city;
        response.preferredPaymentMethod = preferredPaymentMethod;
        return response;
    }

    /**
     * Create failed authentication response
     */
    public static AuthResponse failure(String message) {
        AuthResponse response = new AuthResponse();
        response.success = false;
        response.message = message;
        return response;
    }

    /**
     * Check if authentication was successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Check if authentication failed
     */
    public boolean isFailure() {
        return !success;
    }
}