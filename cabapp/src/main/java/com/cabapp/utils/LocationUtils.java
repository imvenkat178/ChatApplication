package com.cabapp.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Location utility functions for GPS coordinates
 * Provides formatting, parsing, and validation utilities
 */
@Slf4j
public class LocationUtils {

    private static final DecimalFormat COORDINATE_FORMAT = new DecimalFormat("#.######");
    private static final DecimalFormat DISTANCE_FORMAT = new DecimalFormat("#.##");

    // Regex patterns for coordinate validation
    private static final Pattern LAT_PATTERN = Pattern.compile("^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)$");
    private static final Pattern LNG_PATTERN = Pattern.compile("^[-+]?(180(\\.0+)?|((1[0-7]\\d)|([1-9]?\\d))(\\.\\d+)?)$");

    // Atlanta landmarks for reference
    public static final GeoUtils.Coordinates ATLANTA_DOWNTOWN = new GeoUtils.Coordinates(33.7490, -84.3880);
    public static final GeoUtils.Coordinates ATLANTA_AIRPORT = new GeoUtils.Coordinates(33.6407, -84.4277);
    public static final GeoUtils.Coordinates ATLANTA_BUCKHEAD = new GeoUtils.Coordinates(33.8470, -84.3620);
    public static final GeoUtils.Coordinates ATLANTA_MIDTOWN = new GeoUtils.Coordinates(33.7844, -84.3842);
    public static final GeoUtils.Coordinates SANDY_SPRINGS = new GeoUtils.Coordinates(33.9304, -84.5511);

    /**
     * Format coordinates for display
     */
    public static String formatCoordinates(double latitude, double longitude) {
        return String.format("%s, %s",
                COORDINATE_FORMAT.format(latitude),
                COORDINATE_FORMAT.format(longitude));
    }

    /**
     * Format coordinates with cardinal directions
     */
    public static String formatCoordinatesWithCardinal(double latitude, double longitude) {
        String latDir = latitude >= 0 ? "N" : "S";
        String lngDir = longitude >= 0 ? "E" : "W";

        return String.format("%s°%s, %s°%s",
                COORDINATE_FORMAT.format(Math.abs(latitude)), latDir,
                COORDINATE_FORMAT.format(Math.abs(longitude)), lngDir);
    }

    /**
     * Parse coordinates from string format "lat,lng"
     */
    public static GeoUtils.Coordinates parseCoordinates(String coordinateString) {
        if (coordinateString == null || coordinateString.trim().isEmpty()) {
            return null;
        }

        try {
            String[] parts = coordinateString.split(",");
            if (parts.length != 2) {
                return null;
            }

            double lat = Double.parseDouble(parts[0].trim());
            double lng = Double.parseDouble(parts[1].trim());

            if (GeoUtils.isValidCoordinates(lat, lng)) {
                return new GeoUtils.Coordinates(lat, lng);
            }

        } catch (NumberFormatException e) {
            log.warn("Invalid coordinate format: {}", coordinateString);
        }

        return null;
    }

    /**
     * Validate coordinate string format
     */
    public static boolean isValidCoordinateString(String latitude, String longitude) {
        if (latitude == null || longitude == null) {
            return false;
        }

        return LAT_PATTERN.matcher(latitude.trim()).matches() &&
                LNG_PATTERN.matcher(longitude.trim()).matches();
    }

    /**
     * Get nearest Atlanta landmark
     */
    public static String getNearestLandmark(double latitude, double longitude) {
        if (!GeoUtils.isValidCoordinates(latitude, longitude)) {
            return "Invalid location";
        }

        if (!GeoUtils.isInAtlantaServiceArea(latitude, longitude)) {
            return "Outside Atlanta area";
        }

        double minDistance = Double.MAX_VALUE;
        String nearestLandmark = "Unknown area";

        // Check distances to major landmarks
        double distanceToDowntown = GeoUtils.calculateDistance(latitude, longitude,
                ATLANTA_DOWNTOWN.latitude, ATLANTA_DOWNTOWN.longitude);
        if (distanceToDowntown < minDistance) {
            minDistance = distanceToDowntown;
            nearestLandmark = "Downtown Atlanta";
        }

        double distanceToAirport = GeoUtils.calculateDistance(latitude, longitude,
                ATLANTA_AIRPORT.latitude, ATLANTA_AIRPORT.longitude);
        if (distanceToAirport < minDistance) {
            minDistance = distanceToAirport;
            nearestLandmark = "Hartsfield-Jackson Airport";
        }

        double distanceToBuckhead = GeoUtils.calculateDistance(latitude, longitude,
                ATLANTA_BUCKHEAD.latitude, ATLANTA_BUCKHEAD.longitude);
        if (distanceToBuckhead < minDistance) {
            minDistance = distanceToBuckhead;
            nearestLandmark = "Buckhead";
        }

        double distanceToMidtown = GeoUtils.calculateDistance(latitude, longitude,
                ATLANTA_MIDTOWN.latitude, ATLANTA_MIDTOWN.longitude);
        if (distanceToMidtown < minDistance) {
            minDistance = distanceToMidtown;
            nearestLandmark = "Midtown";
        }

        double distanceToSandySprings = GeoUtils.calculateDistance(latitude, longitude,
                SANDY_SPRINGS.latitude, SANDY_SPRINGS.longitude);
        if (distanceToSandySprings < minDistance) {
            minDistance = distanceToSandySprings;
            nearestLandmark = "Sandy Springs";
        }

        return String.format("%s (%.1f km)", nearestLandmark, minDistance);
    }

    /**
     * Get location description
     */
    public static String getLocationDescription(double latitude, double longitude) {
        if (!GeoUtils.isValidCoordinates(latitude, longitude)) {
            return "Invalid coordinates";
        }

        StringBuilder description = new StringBuilder();
        description.append(formatCoordinates(latitude, longitude));

        if (GeoUtils.isInAtlantaServiceArea(latitude, longitude)) {
            description.append(" - ").append(getNearestLandmark(latitude, longitude));
        } else {
            description.append(" - Outside Atlanta service area");
        }

        return description.toString();
    }

    /**
     * Create bounding box around a center point
     */
    public static BoundingBox createBoundingBox(double centerLat, double centerLng, double radiusKm) {
        // Approximate degrees per kilometer (varies by latitude)
        double latDegreesPerKm = 1.0 / 111.0;
        double lngDegreesPerKm = 1.0 / (111.0 * Math.cos(Math.toRadians(centerLat)));

        double latOffset = radiusKm * latDegreesPerKm;
        double lngOffset = radiusKm * lngDegreesPerKm;

        return new BoundingBox(
                centerLat - latOffset, // minLat
                centerLat + latOffset, // maxLat
                centerLng - lngOffset, // minLng
                centerLng + lngOffset  // maxLng
        );
    }

    /**
     * Check if coordinates are within bounding box
     */
    public static boolean isWithinBoundingBox(double lat, double lng, BoundingBox box) {
        return lat >= box.minLat && lat <= box.maxLat &&
                lng >= box.minLng && lng <= box.maxLng;
    }

    /**
     * Normalize coordinates to ensure they're within valid ranges
     */
    public static GeoUtils.Coordinates normalizeCoordinates(double latitude, double longitude) {
        // Clamp latitude to [-90, 90]
        latitude = Math.max(-90.0, Math.min(90.0, latitude));

        // Normalize longitude to [-180, 180]
        while (longitude > 180.0) longitude -= 360.0;
        while (longitude < -180.0) longitude += 360.0;

        return new GeoUtils.Coordinates(latitude, longitude);
    }

    /**
     * Convert coordinates to grid cell (for spatial indexing)
     */
    public static GridCell toGridCell(double latitude, double longitude, double gridSizeKm) {
        // Convert to grid coordinates
        int latGrid = (int) Math.floor(latitude / (gridSizeKm / 111.0));
        int lngGrid = (int) Math.floor(longitude / (gridSizeKm / (111.0 * Math.cos(Math.toRadians(latitude)))));

        return new GridCell(latGrid, lngGrid);
    }

    /**
     * Get all grid cells within radius
     */
    public static List<GridCell> getGridCellsInRadius(double centerLat, double centerLng,
                                                      double radiusKm, double gridSizeKm) {
        List<GridCell> cells = new ArrayList<>();
        GridCell centerCell = toGridCell(centerLat, centerLng, gridSizeKm);

        int gridRadius = (int) Math.ceil(radiusKm / gridSizeKm);

        for (int latOffset = -gridRadius; latOffset <= gridRadius; latOffset++) {
            for (int lngOffset = -gridRadius; lngOffset <= gridRadius; lngOffset++) {
                cells.add(new GridCell(
                        centerCell.latGrid + latOffset,
                        centerCell.lngGrid + lngOffset
                ));
            }
        }

        return cells;
    }

    /**
     * Get quadrant for coordinate (NE, NW, SE, SW relative to Atlanta center)
     */
    public static String getAtlantaQuadrant(double latitude, double longitude) {
        GeoUtils.Coordinates center = GeoUtils.getAtlantaCenter();

        boolean north = latitude > center.latitude;
        boolean east = longitude > center.longitude;

        if (north && east) return "NE";
        if (north && !east) return "NW";
        if (!north && east) return "SE";
        return "SW";
    }

    /**
     * Calculate GPS accuracy category
     */
    public static String getAccuracyCategory(double accuracyMeters) {
        if (accuracyMeters <= 5) return "Excellent";
        if (accuracyMeters <= 10) return "Good";
        if (accuracyMeters <= 20) return "Fair";
        if (accuracyMeters <= 50) return "Poor";
        return "Very Poor";
    }

    /**
     * Generate test coordinates for Atlanta neighborhoods
     */
    public static List<GeoUtils.Coordinates> getAtlantaTestCoordinates() {
        List<GeoUtils.Coordinates> coordinates = new ArrayList<>();

        // Major Atlanta areas
        coordinates.add(ATLANTA_DOWNTOWN);
        coordinates.add(ATLANTA_MIDTOWN);
        coordinates.add(ATLANTA_BUCKHEAD);
        coordinates.add(ATLANTA_AIRPORT);
        coordinates.add(SANDY_SPRINGS);

        // Additional neighborhoods
        coordinates.add(new GeoUtils.Coordinates(33.7968, -84.3206)); // Virginia-Highland
        coordinates.add(new GeoUtils.Coordinates(33.7680, -84.4205)); // West Midtown
        coordinates.add(new GeoUtils.Coordinates(33.6612, -84.2979)); // Decatur
        coordinates.add(new GeoUtils.Coordinates(33.8151, -84.1520)); // Stone Mountain
        coordinates.add(new GeoUtils.Coordinates(33.8873, -84.4647)); // Sandy Springs

        return coordinates;
    }

    /**
     * Bounding box class
     */
    public static class BoundingBox {
        public final double minLat;
        public final double maxLat;
        public final double minLng;
        public final double maxLng;

        public BoundingBox(double minLat, double maxLat, double minLng, double maxLng) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLng = minLng;
            this.maxLng = maxLng;
        }

        public GeoUtils.Coordinates getCenter() {
            return new GeoUtils.Coordinates((minLat + maxLat) / 2, (minLng + maxLng) / 2);
        }

        public boolean contains(double lat, double lng) {
            return isWithinBoundingBox(lat, lng, this);
        }

        @Override
        public String toString() {
            return String.format("BoundingBox[%.6f,%.6f to %.6f,%.6f]", minLat, minLng, maxLat, maxLng);
        }
    }

    /**
     * Grid cell class for spatial indexing
     */
    public static class GridCell {
        public final int latGrid;
        public final int lngGrid;

        public GridCell(int latGrid, int lngGrid) {
            this.latGrid = latGrid;
            this.lngGrid = lngGrid;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            GridCell gridCell = (GridCell) obj;
            return latGrid == gridCell.latGrid && lngGrid == gridCell.lngGrid;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(latGrid, lngGrid);
        }

        @Override
        public String toString() {
            return String.format("GridCell[%d,%d]", latGrid, lngGrid);
        }
    }
}