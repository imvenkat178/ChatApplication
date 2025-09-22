package com.cabapp.utils;

/**
 * Application-wide constants for the cab booking system
 * Contains configuration values, limits, and default settings
 */
public final class Constants {

    // Prevent instantiation
    private Constants() {}

    /**
     * Service area constants
     */
    public static final class ServiceArea {
        public static final String CITY = "Atlanta";
        public static final String STATE = "Georgia";
        public static final String COUNTRY = "USA";
        public static final String TIMEZONE = "America/New_York";

        // Atlanta service area bounds
        public static final double MIN_LATITUDE = 33.4;
        public static final double MAX_LATITUDE = 34.1;
        public static final double MIN_LONGITUDE = -84.8;
        public static final double MAX_LONGITUDE = -84.0;
        public static final double SERVICE_RADIUS_KM = 50.0;

        // Center coordinates
        public static final double CENTER_LATITUDE = 33.7490;
        public static final double CENTER_LONGITUDE = -84.3880;
    }

    /**
     * Ride booking constants
     */
    public static final class Ride {
        public static final int MAX_SEARCH_RADIUS_KM = 10;
        public static final int DEFAULT_SEARCH_RADIUS_KM = 5;
        public static final int MAX_RIDE_DURATION_HOURS = 12;
        public static final int RIDE_TIMEOUT_MINUTES = 15; // Auto-cancel if no driver found
        public static final int DRIVER_ARRIVAL_TIMEOUT_MINUTES = 30;

        // Fare calculation
        public static final double BASE_FARE = 50.0; // Minimum fare in INR
        public static final double BOOKING_FEE = 10.0;
        public static final double CANCELLATION_FEE_MIN = 20.0;
        public static final double CANCELLATION_FEE_MAX = 100.0;
        public static final int FREE_CANCELLATION_MINUTES = 2;

        // Distance thresholds
        public static final double MIN_RIDE_DISTANCE_KM = 0.5;
        public static final double MAX_RIDE_DISTANCE_KM = 100.0;
        public static final double LONG_DISTANCE_THRESHOLD_KM = 50.0;
    }

    /**
     * Driver constants
     */
    public static final class Driver {
        public static final int MAX_DRIVERS_TO_NOTIFY = 5;
        public static final int DRIVER_RESPONSE_TIMEOUT_SECONDS = 30;
        public static final int LOCATION_UPDATE_INTERVAL_SECONDS = 5;
        public static final int LOCATION_STALENESS_MINUTES = 10; // Consider location stale after this
        public static final int MAX_CONCURRENT_RIDES = 1; // One ride per driver

        // Movement simulation
        public static final double MAX_MOVEMENT_SPEED_KMH = 60.0;
        public static final double MIN_MOVEMENT_DISTANCE_KM = 0.1;
        public static final double MAX_MOVEMENT_DISTANCE_KM = 2.0;
        public static final int SIMULATION_UPDATE_INTERVAL_MS = 5000;
    }

    /**
     * Location and GPS constants
     */
    public static final class Location {
        public static final double EARTH_RADIUS_KM = 6371.0;
        public static final double GPS_ACCURACY_THRESHOLD_METERS = 50.0;
        public static final int COORDINATE_PRECISION = 6; // Decimal places
        public static final double MIN_COORDINATE_CHANGE = 0.0001; // ~10 meters

        // Geocoding
        public static final int GEOCODING_TIMEOUT_SECONDS = 10;
        public static final int MAX_GEOCODING_RETRIES = 3;
        public static final String DEFAULT_ADDRESS = "Unknown Address";
    }

    /**
     * Kafka topic names
     */
    public static final class KafkaTopics {
        public static final String DRIVER_LOCATIONS = "driver-locations";
        public static final String RIDE_EVENTS = "ride-events";
        public static final String USER_NOTIFICATIONS = "user-notifications";
        public static final String DRIVER_NOTIFICATIONS = "driver-notifications";
        public static final String EMERGENCY_ALERTS = "emergency-alerts";
        public static final String SYSTEM_EVENTS = "system-events";
    }

    /**
     * Redis keys
     */
    public static final class RedisKeys {
        public static final String DRIVER_LOCATIONS = "driver_locations";
        public static final String USER_SESSIONS = "user_sessions";
        public static final String DRIVER_SESSIONS = "driver_sessions";
        public static final String RIDE_CACHE = "ride_cache";
        public static final String RATE_LIMITING = "rate_limiting";
        public static final String GEOFENCE = "geofence";
    }

    /**
     * WebSocket endpoints
     */
    public static final class WebSocketEndpoints {
        public static final String USER_NOTIFICATIONS = "/topic/notifications";
        public static final String DRIVER_NOTIFICATIONS = "/topic/driver-notifications";
        public static final String DRIVER_LOCATION = "/topic/driver-location";
        public static final String SYSTEM_STATUS = "/topic/system-status";
        public static final String EMERGENCY = "/topic/emergency";
        public static final String ADMIN_DASHBOARD = "/topic/admin";
    }

    /**
     * API endpoints
     */
    public static final class ApiEndpoints {
        public static final String API_BASE = "/api";

        // Authentication
        public static final String AUTH_LOGIN = "/api/auth/login";
        public static final String AUTH_REGISTER = "/api/auth/register";
        public static final String AUTH_LOGOUT = "/api/auth/logout";

        // Rides
        public static final String RIDES = "/api/rides";
        public static final String RIDE_BY_ID = "/api/rides/{id}";
        public static final String RIDE_ESTIMATE = "/api/rides/estimate";

        // Drivers
        public static final String DRIVERS = "/api/drivers";
        public static final String DRIVERS_NEARBY = "/api/drivers/nearby";

        // Locations
        public static final String LOCATIONS = "/api/locations";
        public static final String LOCATION_VALIDATE = "/api/locations/validate-pickup";
        public static final String LOCATION_GEOCODE = "/api/locations/geocode-dropoff";
    }

    /**
     * Time constants
     */
    public static final class Time {
        public static final int SECONDS_PER_MINUTE = 60;
        public static final int MINUTES_PER_HOUR = 60;
        public static final int HOURS_PER_DAY = 24;
        public static final int MILLISECONDS_PER_SECOND = 1000;

        // Peak hours
        public static final int MORNING_PEAK_START = 6; // 6 AM
        public static final int MORNING_PEAK_END = 10;   // 10 AM
        public static final int EVENING_PEAK_START = 17; // 5 PM
        public static final int EVENING_PEAK_END = 21;   // 9 PM
        public static final int NIGHT_START = 22;        // 10 PM
        public static final int NIGHT_END = 6;           // 6 AM
    }

    /**
     * Fare multipliers
     */
    public static final class FareMultipliers {
        public static final double PEAK_HOUR = 1.5;
        public static final double NIGHT_TIME = 1.3;
        public static final double WEEKEND = 1.2;
        public static final double LONG_DISTANCE_DISCOUNT = 0.9; // 10% discount
        public static final double SURGE_MAX = 3.0; // Maximum surge pricing
    }

    /**
     * Vehicle type constants
     */
    public static final class VehicleTypes {
        public static final String MINI = "MINI";
        public static final String HATCHBACK = "HATCHBACK";
        public static final String SEDAN = "SEDAN";
        public static final String SUV = "SUV";
        public static final String PREMIUM = "PREMIUM";
        public static final String LUXURY = "LUXURY";
        public static final String AUTO_RICKSHAW = "AUTO_RICKSHAW";
        public static final String BIKE = "BIKE";
    }

    /**
     * Payment method constants
     */
    public static final class PaymentMethods {
        public static final String CASH = "CASH";
        public static final String UPI = "UPI";
        public static final String CARD = "CARD";
        public static final String WALLET = "WALLET";
        public static final String NET_BANKING = "NET_BANKING";
    }

    /**
     * Notification types
     */
    public static final class NotificationTypes {
        public static final String RIDE_CREATED = "RIDE_CREATED";
        public static final String DRIVER_ASSIGNED = "DRIVER_ASSIGNED";
        public static final String DRIVER_ARRIVED = "DRIVER_ARRIVED";
        public static final String RIDE_STARTED = "RIDE_STARTED";
        public static final String RIDE_COMPLETED = "RIDE_COMPLETED";
        public static final String RIDE_CANCELLED = "RIDE_CANCELLED";
        public static final String LOCATION_PERMISSION_REQUIRED = "LOCATION_PERMISSION_REQUIRED";
        public static final String GEOCODING_ERROR = "GEOCODING_ERROR";
        public static final String EMERGENCY = "EMERGENCY";
        public static final String SYSTEM_STATUS = "SYSTEM_STATUS";
    }

    /**
     * Error messages
     */
    public static final class ErrorMessages {
        public static final String INVALID_COORDINATES = "Invalid coordinates provided";
        public static final String OUTSIDE_SERVICE_AREA = "Location is outside Atlanta service area";
        public static final String LOCATION_PERMISSION_DENIED = "Location permission is required for pickup";
        public static final String GEOCODING_FAILED = "Unable to find location for provided address";
        public static final String NO_DRIVERS_AVAILABLE = "No drivers available in your area";
        public static final String RIDE_ALREADY_EXISTS = "You already have an active ride";
        public static final String DRIVER_NOT_AVAILABLE = "Selected driver is not available";
        public static final String INVALID_RIDE_STATUS = "Invalid ride status for this operation";
        public static final String PAYMENT_FAILED = "Payment processing failed";
        public static final String USER_NOT_FOUND = "User not found";
        public static final String DRIVER_NOT_FOUND = "Driver not found";
        public static final String RIDE_NOT_FOUND = "Ride not found";
    }

    /**
     * Success messages
     */
    public static final class SuccessMessages {
        public static final String RIDE_CREATED = "Ride booked successfully";
        public static final String DRIVER_ASSIGNED = "Driver assigned to your ride";
        public static final String RIDE_STARTED = "Your ride has started";
        public static final String RIDE_COMPLETED = "Ride completed successfully";
        public static final String PAYMENT_SUCCESSFUL = "Payment processed successfully";
        public static final String LOCATION_UPDATED = "Location updated successfully";
        public static final String DRIVER_ARRIVED = "Driver has arrived at pickup location";
    }

    /**
     * Validation patterns
     */
    public static final class ValidationPatterns {
        public static final String EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$";
        public static final String PHONE = "^\\+?[1-9]\\d{1,14}$";
        public static final String LATITUDE = "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)$";
        public static final String LONGITUDE = "^[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$";
        public static final String DRIVER_ID = "^(driver_|sim_driver_)[a-zA-Z0-9_-]+$";
        public static final String USER_ID = "^user_[a-zA-Z0-9_-]+$";
        public static final String RIDE_ID = "^\\d+$";
    }

    /**
     * Cache expiration times (in seconds)
     */
    public static final class CacheExpiration {
        public static final int DRIVER_LOCATION = 300; // 5 minutes
        public static final int RIDE_DATA = 1800; // 30 minutes
        public static final int USER_SESSION = 3600; // 1 hour
        public static final int GEOCODING_RESULT = 86400; // 24 hours
        public static final int FARE_ESTIMATE = 600; // 10 minutes
    }

    /**
     * Rate limiting constants
     */
    public static final class RateLimiting {
        public static final int RIDE_REQUESTS_PER_HOUR = 10;
        public static final int LOCATION_UPDATES_PER_MINUTE = 60;
        public static final int LOGIN_ATTEMPTS_PER_MINUTE = 5;
        public static final int GEOCODING_REQUESTS_PER_MINUTE = 100;
        public static final int API_REQUESTS_PER_MINUTE = 1000;
    }

    /**
     * Database constants
     */
    public static final class Database {
        public static final int CONNECTION_TIMEOUT_SECONDS = 30;
        public static final int MAX_POOL_SIZE = 20;
        public static final int MIN_POOL_SIZE = 5;
        public static final String DEFAULT_SCHEMA = "cabapp";

        // Table names
        public static final String TABLE_USERS = "users";
        public static final String TABLE_DRIVERS = "drivers";
        public static final String TABLE_RIDES = "rides";
        public static final String TABLE_PAYMENTS = "payments";
    }

    /**
     * Monitoring and logging
     */
    public static final class Monitoring {
        public static final String HEALTH_CHECK_ENDPOINT = "/actuator/health";
        public static final String METRICS_ENDPOINT = "/actuator/metrics";
        public static final int HEALTH_CHECK_INTERVAL_SECONDS = 30;
        public static final int LOG_CLEANUP_DAYS = 30;

        // Alert thresholds
        public static final double CPU_USAGE_ALERT_THRESHOLD = 80.0;
        public static final double MEMORY_USAGE_ALERT_THRESHOLD = 85.0;
        public static final int ERROR_RATE_ALERT_THRESHOLD = 5; // errors per minute
    }

    /**
     * Security constants
     */
    public static final class Security {
        public static final int PASSWORD_MIN_LENGTH = 6;
        public static final int PASSWORD_MAX_LENGTH = 128;
        public static final int SESSION_TIMEOUT_HOURS = 24;
        public static final int MAX_LOGIN_ATTEMPTS = 5;
        public static final int ACCOUNT_LOCKOUT_MINUTES = 30;

        // JWT (if using JWT tokens)
        public static final String JWT_SECRET_KEY = "cab_booking_secret_key_change_in_production";
        public static final int JWT_EXPIRATION_HOURS = 24;
    }

    /**
     * Feature flags
     */
    public static final class FeatureFlags {
        public static final boolean DRIVER_SIMULATION_ENABLED = true;
        public static final boolean LOCATION_VALIDATION_STRICT = true;
        public static final boolean FARE_SURGE_PRICING_ENABLED = false;
        public static final boolean PROMO_CODES_ENABLED = true;
        public static final boolean SCHEDULED_RIDES_ENABLED = false;
        public static final boolean EMERGENCY_BUTTON_ENABLED = true;
        public static final boolean RATING_SYSTEM_ENABLED = false;
    }

    /**
     * Default values
     */
    public static final class Defaults {
        public static final String DEFAULT_PAYMENT_METHOD = PaymentMethods.CASH;
        public static final String DEFAULT_VEHICLE_TYPE = VehicleTypes.HATCHBACK;
        public static final String DEFAULT_CURRENCY = "INR";
        public static final String DEFAULT_LANGUAGE = "en";
        public static final double DEFAULT_DRIVER_RATING = 4.5;
        public static final int DEFAULT_ESTIMATED_ARRIVAL_MINUTES = 10;
        public static final double DEFAULT_CITY_SPEED_KMH = 40.0;
    }
}