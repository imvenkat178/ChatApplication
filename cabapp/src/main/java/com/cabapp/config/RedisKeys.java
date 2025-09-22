package com.cabapp.config;

/**
 * Redis Key Constants for Cab Booking System
 */
public class RedisKeys {

    // Geospatial keys for driver locations
    public static final String GEO_AVAILABLE_DRIVERS = "geo:drivers:available";
    public static final String GEO_ALL_DRIVERS = "geo:drivers:all";

    // Driver information keys
    private static final String DRIVER_PREFIX = "driver:";

    // Ride information keys
    private static final String RIDE_PREFIX = "ride:";

    // Session keys
    private static final String SESSION_PREFIX = "session:";

    /**
     * Generate driver-specific Redis key
     */
    public static String driverKey(String driverId) {
        return DRIVER_PREFIX + driverId;
    }

    /**
     * Generate ride-specific Redis key
     */
    public static String rideKey(String rideId) {
        return RIDE_PREFIX + rideId;
    }

    /**
     * Generate session-specific Redis key
     */
    public static String sessionKey(String sessionId) {
        return SESSION_PREFIX + sessionId;
    }

    /**
     * Driver location history key
     */
    public static String driverLocationHistory(String driverId) {
        return DRIVER_PREFIX + driverId + ":locations";
    }

    /**
     * Active rides key for a driver
     */
    public static String driverActiveRides(String driverId) {
        return DRIVER_PREFIX + driverId + ":active-rides";
    }

    /**
     * Ride tracking key
     */
    public static String rideTracking(String rideId) {
        return RIDE_PREFIX + rideId + ":tracking";
    }
}