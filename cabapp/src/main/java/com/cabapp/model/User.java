package com.cabapp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private String city;
    private String state;
    private String country;

    private Boolean locationPermissionGranted = false;

    @Enumerated(EnumType.STRING)
    private PaymentMethod preferredPaymentMethod = PaymentMethod.CASH;

    private String preferredDeviceType = "MOBILE";
    private Boolean notificationsEnabled = true;

    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastLoginAt = LocalDateTime.now();
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * Check if location permission is enabled
     */
    public boolean isLocationPermissionEnabled() {
        return Boolean.TRUE.equals(locationPermissionGranted);
    }

    /**
     * Set location permission
     */
    public void setLocationPermissionGranted(Boolean granted) {
        this.locationPermissionGranted = granted;
    }

    /**
     * Get location permission status
     */
    public Boolean getLocationPermissionGranted() {
        return locationPermissionGranted;
    }

    // Additional getters and setters for Builder pattern compatibility
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
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

    public UserStatus getStatus() {
        return status;
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

    public PaymentMethod getPreferredPaymentMethod() {
        return preferredPaymentMethod;
    }

    public String getPreferredDeviceType() {
        return preferredDeviceType;
    }

    public Boolean getNotificationsEnabled() {
        return notificationsEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}