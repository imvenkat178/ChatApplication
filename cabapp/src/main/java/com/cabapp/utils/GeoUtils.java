package com.cabapp.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * Geospatial utility functions for the cab booking system
 * Provides distance calculations, coordinate validation, and Atlanta service area checks
 */
@Slf4j
public class GeoUtils {

    // Earth's radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;

    // Atlanta service area bounds
    private static final double ATLANTA_MIN_LAT = 33.4;
    private static final double ATLANTA_MAX_LAT = 34.1;
    private static final double ATLANTA_MIN_LNG = -84.8;
    private static final double ATLANTA_MAX_LNG = -84.0;

    // Coordinate validation constants
    private static final double MIN_LATITUDE = -90.0;
    private static final double MAX_LATITUDE = 90.0;
    private static final double MIN_LONGITUDE = -180.0;
    private static final double MAX_LONGITUDE = 180.0;

    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Validate coordinates
        if (!isValidCoordinates(lat1, lon1) || !isValidCoordinates(lat2, lon2)) {
            log.error("Invalid coordinates: ({}, {}) to ({}, {})", lat1, lon1, lat2, lon2);
            return -1.0;
        }

        // Convert latitude and longitude from degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Haversine formula
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculate distance and return formatted string
     */
    public static String calculateDistanceFormatted(double lat1, double lon1, double lat2, double lon2) {
        double distance = calculateDistance(lat1, lon1, lat2, lon2);
        if (distance < 0) {
            return "Invalid coordinates";
        }

        if (distance < 1.0) {
            return String.format("%.0f meters", distance * 1000);
        } else {
            return String.format("%.1f km", distance);
        }
    }

    /**
     * Validate if coordinates are within valid ranges
     */
    public static boolean isValidCoordinates(double latitude, double longitude) {
        return latitude >= MIN_LATITUDE && latitude <= MAX_LATITUDE &&
                longitude >= MIN_LONGITUDE && longitude <= MAX_LONGITUDE;
    }

    /**
     * Check if coordinates are within Atlanta service area
     */
    public static boolean isInAtlantaServiceArea(double latitude, double longitude) {
        if (!isValidCoordinates(latitude, longitude)) {
            return false;
        }

        return latitude >= ATLANTA_MIN_LAT && latitude <= ATLANTA_MAX_LAT &&
                longitude >= ATLANTA_MIN_LNG && longitude <= ATLANTA_MAX_LNG;
    }

    /**
     * Get Atlanta service area center coordinates
     */
    public static Coordinates getAtlantaCenter() {
        double centerLat = (ATLANTA_MIN_LAT + ATLANTA_MAX_LAT) / 2;
        double centerLng = (ATLANTA_MIN_LNG + ATLANTA_MAX_LNG) / 2;
        return new Coordinates(centerLat, centerLng);
    }

    /**
     * Calculate bearing (direction) from point A to point B
     * @return Bearing in degrees (0-360)
     */
    public static double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        if (!isValidCoordinates(lat1, lon1) || !isValidCoordinates(lat2, lon2)) {
            return -1.0;
        }

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad);

        double bearingRad = Math.atan2(y, x);
        double bearingDeg = Math.toDegrees(bearingRad);

        // Normalize to 0-360 degrees
        return (bearingDeg + 360) % 360;
    }

    /**
     * Get compass direction from bearing
     */
    public static String getCompassDirection(double bearing) {
        if (bearing < 0) return "Unknown";

        String[] directions = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
                "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};

        int index = (int) Math.round(bearing / 22.5) % 16;
        return directions[index];
    }

    /**
     * Calculate estimated travel time based on distance and speed
     * @param distanceKm Distance in kilometers
     * @param speedKmh Average speed in km/h (default: 40 km/h for city driving)
     * @return Estimated time in minutes
     */
    public static int calculateEstimatedTravelTime(double distanceKm, double speedKmh) {
        if (distanceKm <= 0 || speedKmh <= 0) {
            return 0;
        }

        double timeHours = distanceKm / speedKmh;
        return (int) Math.ceil(timeHours * 60); // Convert to minutes and round up
    }

    /**
     * Calculate estimated travel time with default city speed (40 km/h)
     */
    public static int calculateEstimatedTravelTime(double distanceKm) {
        return calculateEstimatedTravelTime(distanceKm, 40.0); // Default city speed
    }

    /**
     * Check if point is within radius of center point
     */
    public static boolean isWithinRadius(double centerLat, double centerLng,
                                         double pointLat, double pointLng,
                                         double radiusKm) {
        double distance = calculateDistance(centerLat, centerLng, pointLat, pointLng);
        return distance >= 0 && distance <= radiusKm;
    }

    /**
     * Generate random coordinates within Atlanta service area (for testing/simulation)
     */
    public static Coordinates generateRandomAtlantaCoordinates() {
        double lat = ATLANTA_MIN_LAT + Math.random() * (ATLANTA_MAX_LAT - ATLANTA_MIN_LAT);
        double lng = ATLANTA_MIN_LNG + Math.random() * (ATLANTA_MAX_LNG - ATLANTA_MIN_LNG);
        return new Coordinates(lat, lng);
    }

    /**
     * Move coordinates by distance and bearing (for simulation)
     */
    public static Coordinates moveCoordinates(double lat, double lng, double distanceKm, double bearingDegrees) {
        if (!isValidCoordinates(lat, lng) || distanceKm < 0) {
            return null;
        }

        double latRad = Math.toRadians(lat);
        double lngRad = Math.toRadians(lng);
        double bearingRad = Math.toRadians(bearingDegrees);

        double angularDistance = distanceKm / EARTH_RADIUS_KM;

        double newLatRad = Math.asin(Math.sin(latRad) * Math.cos(angularDistance) +
                Math.cos(latRad) * Math.sin(angularDistance) * Math.cos(bearingRad));

        double newLngRad = lngRad + Math.atan2(Math.sin(bearingRad) * Math.sin(angularDistance) * Math.cos(latRad),
                Math.cos(angularDistance) - Math.sin(latRad) * Math.sin(newLatRad));

        double newLat = Math.toDegrees(newLatRad);
        double newLng = Math.toDegrees(newLngRad);

        return new Coordinates(newLat, newLng);
    }

    /**
     * Get Atlanta service area bounds
     */
    public static ServiceAreaBounds getAtlantaBounds() {
        return new ServiceAreaBounds(ATLANTA_MIN_LAT, ATLANTA_MAX_LAT, ATLANTA_MIN_LNG, ATLANTA_MAX_LNG);
    }

    /**
     * Helper class for coordinates
     */
    public static class Coordinates {
        public final double latitude;
        public final double longitude;

        public Coordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return String.format("(%.6f, %.6f)", latitude, longitude);
        }

        public boolean isValid() {
            return isValidCoordinates(latitude, longitude);
        }

        public boolean isInAtlanta() {
            return isInAtlantaServiceArea(latitude, longitude);
        }
    }

    /**
     * Helper class for service area bounds
     */
    public static class ServiceAreaBounds {
        public final double minLat;
        public final double maxLat;
        public final double minLng;
        public final double maxLng;

        public ServiceAreaBounds(double minLat, double maxLat, double minLng, double maxLng) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLng = minLng;
            this.maxLng = maxLng;
        }

        public boolean contains(double lat, double lng) {
            return lat >= minLat && lat <= maxLat && lng >= minLng && lng <= maxLng;
        }

        public Coordinates getCenter() {
            return new Coordinates((minLat + maxLat) / 2, (minLng + maxLng) / 2);
        }
    }
}