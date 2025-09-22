package com.cabapp.model;

/**
 * User Status Enum
 * 
 * Represents the current status of a user account
 * Used for account management and access control
 */
public enum UserStatus {
    
    /**
     * User account is active and can book rides
     */
    ACTIVE("Active user account"),
    
    /**
     * User account is temporarily inactive but not suspended
     */
    INACTIVE("Inactive user account"),
    
    /**
     * User account is suspended and cannot book rides
     */
    SUSPENDED("Suspended user account");
    
    private final String description;
    
    UserStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if user can book rides
     */
    public boolean canBookRides() {
        return this == ACTIVE;
    }
    
    /**
     * Check if user can login
     */
    public boolean canLogin() {
        return this == ACTIVE || this == INACTIVE;
    }
}