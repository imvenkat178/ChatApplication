package com.cabapp.model;

import lombok.Getter;

/**
 * Enhanced Driver Status Enum
 *
 * Represents all possible states of a driver in the cab booking system
 * Used for filtering available drivers in Redis GEORADIUS queries
 * Updated automatically during ride lifecycle and driver simulation
 */
@Getter
public enum DriverStatus {

    /**
     * Driver is not logged into the app
     * Removed from Redis geospatial index
     */
    OFFLINE("Offline", 0, false, false),

    /**
     * Driver is online and ready to accept rides
     * Included in Redis geospatial queries for ride assignment
     */
    AVAILABLE("Available", 1, true, true),

    /**
     * Driver has been assigned a ride and heading to pickup
     * Excluded from new ride assignments but location still tracked
     */
    ASSIGNED("Assigned to Ride", 2, false, true),

    /**
     * Driver is currently on an active ride with passenger
     * Excluded from Redis geospatial queries until ride completes
     */
    BUSY("On Trip", 3, false, true),

    /**
     * Driver is online but taking a break
     * Excluded from ride assignments but location still tracked
     */
    BREAK("On Break", 4, false, true);

    private final String displayName;
    private final int priority; // Higher priority = more likely to get rides
    private final boolean canAcceptRides;
    private final boolean shouldTrackLocation;

    DriverStatus(String displayName, int priority, boolean canAcceptRides, boolean shouldTrackLocation) {
        this.displayName = displayName;
        this.priority = priority;
        this.canAcceptRides = canAcceptRides;
        this.shouldTrackLocation = shouldTrackLocation;
    }

    /**
     * Check if driver can accept new rides
     */
    public boolean canAcceptRides() {
        return canAcceptRides;
    }

    /**
     * Check if driver should be included in location tracking
     */
    public boolean shouldTrackLocation() {
        return shouldTrackLocation;
    }

    /**
     * Check if driver is actively working (not offline)
     */
    public boolean isActive() {
        return this != OFFLINE;
    }

    /**
     * Check if driver is currently on a ride
     */
    public boolean isOnRide() {
        return this == ASSIGNED || this == BUSY;
    }

    /**
     * Check if driver is available for immediate assignment
     */
    public boolean isAvailableForAssignment() {
        return this == AVAILABLE;
    }

    /**
     * Get next logical status after completing a ride
     */
    public DriverStatus getStatusAfterRideCompletion() {
        if (this == BUSY || this == ASSIGNED) {
            return AVAILABLE;
        }
        return this; // No change for other statuses
    }

    /**
     * Get status description for display
     */
    public String getDescription() {
        switch (this) {
            case OFFLINE:
                return "Driver is not accepting rides";
            case AVAILABLE:
                return "Available for new rides";
            case ASSIGNED:
                return "Heading to pickup location";
            case BUSY:
                return "Currently on a ride";
            case BREAK:
                return "Taking a break";
            default:
                return displayName;
        }
    }

    /**
     * Get color code for UI display
     */
    public String getColorCode() {
        switch (this) {
            case OFFLINE:
                return "#6B7280"; // Gray
            case AVAILABLE:
                return "#10B981"; // Green
            case ASSIGNED:
                return "#F59E0B"; // Orange
            case BUSY:
                return "#EF4444"; // Red
            case BREAK:
                return "#8B5CF6"; // Purple
            default:
                return "#6B7280";
        }
    }

    /**
     * Get all statuses that should be included in ride assignment
     */
    public static DriverStatus[] getAssignableStatuses() {
        return new DriverStatus[]{AVAILABLE};
    }

    /**
     * Get all statuses that require location tracking
     */
    public static DriverStatus[] getLocationTrackedStatuses() {
        return new DriverStatus[]{AVAILABLE, ASSIGNED, BUSY, BREAK};
    }

    /**
     * Get all online statuses
     */
    public static DriverStatus[] getOnlineStatuses() {
        return new DriverStatus[]{AVAILABLE, ASSIGNED, BUSY, BREAK};
    }
}