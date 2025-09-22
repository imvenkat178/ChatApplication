package com.cabapp.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Route generation utility for driver simulation
 * Creates realistic movement patterns for simulated drivers in Atlanta
 */
@Slf4j
public class RouteGenerator {

    private static final Random random = new Random();

    // Atlanta major roads and intersections (simplified)
    private static final List<GeoUtils.Coordinates> ATLANTA_HOTSPOTS = List.of(
            new GeoUtils.Coordinates(33.7490, -84.3880), // Downtown Atlanta
            new GeoUtils.Coordinates(33.7844, -84.3842), // Midtown
            new GeoUtils.Coordinates(33.8470, -84.3620), // Buckhead
            new GeoUtils.Coordinates(33.6407, -84.4277), // Hartsfield-Jackson Airport
            new GeoUtils.Coordinates(33.7968, -84.3206), // Virginia-Highland
            new GeoUtils.Coordinates(33.7680, -84.4205), // West Midtown
            new GeoUtils.Coordinates(33.8873, -84.4647), // Sandy Springs
            new GeoUtils.Coordinates(33.9304, -84.5511), // Marietta
            new GeoUtils.Coordinates(33.6612, -84.2979), // Decatur
            new GeoUtils.Coordinates(33.8151, -84.1520)  // Stone Mountain area
    );

    // Movement parameters
    private static final double MIN_MOVE_DISTANCE_KM = 0.1; // 100 meters
    private static final double MAX_MOVE_DISTANCE_KM = 2.0; // 2 kilometers
    private static final double HOTSPOT_ATTRACTION_PROBABILITY = 0.3; // 30% chance to move toward hotspot

    /**
     * Generate next position for a driver based on current location
     */
    public static GeoUtils.Coordinates generateNextPosition(GeoUtils.Coordinates currentPosition) {
        if (currentPosition == null || !currentPosition.isValid()) {
            return GeoUtils.generateRandomAtlantaCoordinates();
        }

        // 30% chance to move toward a hotspot, 70% chance for random movement
        if (random.nextDouble() < HOTSPOT_ATTRACTION_PROBABILITY) {
            return moveTowardHotspot(currentPosition);
        } else {
            return generateRandomMovement(currentPosition);
        }
    }

    /**
     * Move driver toward nearest hotspot
     */
    private static GeoUtils.Coordinates moveTowardHotspot(GeoUtils.Coordinates currentPosition) {
        // Find nearest hotspot
        GeoUtils.Coordinates nearestHotspot = findNearestHotspot(currentPosition);

        // Calculate bearing toward hotspot
        double bearing = GeoUtils.calculateBearing(
                currentPosition.latitude, currentPosition.longitude,
                nearestHotspot.latitude, nearestHotspot.longitude
        );

        // Move a random distance toward the hotspot
        double moveDistance = MIN_MOVE_DISTANCE_KM +
                random.nextDouble() * (MAX_MOVE_DISTANCE_KM - MIN_MOVE_DISTANCE_KM);

        // Add some randomness to the bearing (±30 degrees)
        bearing += (random.nextDouble() - 0.5) * 60;

        GeoUtils.Coordinates newPosition = GeoUtils.moveCoordinates(
                currentPosition.latitude, currentPosition.longitude, moveDistance, bearing
        );

        // Ensure new position is within Atlanta service area
        if (newPosition != null && newPosition.isInAtlanta()) {
            return newPosition;
        } else {
            // Fallback to random movement if moved outside service area
            return generateRandomMovement(currentPosition);
        }
    }

    /**
     * Generate random movement from current position
     */
    private static GeoUtils.Coordinates generateRandomMovement(GeoUtils.Coordinates currentPosition) {
        // Random bearing (0-360 degrees)
        double bearing = random.nextDouble() * 360;

        // Random distance
        double moveDistance = MIN_MOVE_DISTANCE_KM +
                random.nextDouble() * (MAX_MOVE_DISTANCE_KM - MIN_MOVE_DISTANCE_KM);

        GeoUtils.Coordinates newPosition = GeoUtils.moveCoordinates(
                currentPosition.latitude, currentPosition.longitude, moveDistance, bearing
        );

        // If new position is outside Atlanta, move toward center instead
        if (newPosition == null || !newPosition.isInAtlanta()) {
            GeoUtils.Coordinates atlantaCenter = GeoUtils.getAtlantaCenter();
            double bearingToCenter = GeoUtils.calculateBearing(
                    currentPosition.latitude, currentPosition.longitude,
                    atlantaCenter.latitude, atlantaCenter.longitude
            );

            newPosition = GeoUtils.moveCoordinates(
                    currentPosition.latitude, currentPosition.longitude,
                    MIN_MOVE_DISTANCE_KM, bearingToCenter
            );
        }

        return newPosition != null ? newPosition : currentPosition;
    }

    /**
     * Find nearest hotspot to current position
     */
    private static GeoUtils.Coordinates findNearestHotspot(GeoUtils.Coordinates currentPosition) {
        GeoUtils.Coordinates nearest = ATLANTA_HOTSPOTS.get(0);
        double minDistance = GeoUtils.calculateDistance(
                currentPosition.latitude, currentPosition.longitude,
                nearest.latitude, nearest.longitude
        );

        for (GeoUtils.Coordinates hotspot : ATLANTA_HOTSPOTS) {
            double distance = GeoUtils.calculateDistance(
                    currentPosition.latitude, currentPosition.longitude,
                    hotspot.latitude, hotspot.longitude
            );

            if (distance < minDistance) {
                minDistance = distance;
                nearest = hotspot;
            }
        }

        return nearest;
    }

    /**
     * Generate a route with multiple waypoints
     */
    public static List<GeoUtils.Coordinates> generateRoute(GeoUtils.Coordinates start,
                                                           GeoUtils.Coordinates end,
                                                           int waypointCount) {
        List<GeoUtils.Coordinates> route = new ArrayList<>();
        route.add(start);

        if (waypointCount <= 0) {
            route.add(end);
            return route;
        }

        // Generate intermediate waypoints
        for (int i = 1; i <= waypointCount; i++) {
            double progress = (double) i / (waypointCount + 1);

            // Linear interpolation between start and end
            double lat = start.latitude + (end.latitude - start.latitude) * progress;
            double lng = start.longitude + (end.longitude - start.longitude) * progress;

            // Add some randomness to make route more realistic
            double randomOffsetLat = (random.nextDouble() - 0.5) * 0.01; // ±0.01 degrees
            double randomOffsetLng = (random.nextDouble() - 0.5) * 0.01;

            lat += randomOffsetLat;
            lng += randomOffsetLng;

            // Ensure waypoint is within Atlanta service area
            if (GeoUtils.isInAtlantaServiceArea(lat, lng)) {
                route.add(new GeoUtils.Coordinates(lat, lng));
            }
        }

        route.add(end);
        return route;
    }

    /**
     * Generate circular patrol route around a center point
     */
    public static List<GeoUtils.Coordinates> generatePatrolRoute(GeoUtils.Coordinates center,
                                                                 double radiusKm,
                                                                 int pointCount) {
        List<GeoUtils.Coordinates> route = new ArrayList<>();

        double angleStep = 360.0 / pointCount;

        for (int i = 0; i < pointCount; i++) {
            double bearing = i * angleStep;
            GeoUtils.Coordinates point = GeoUtils.moveCoordinates(
                    center.latitude, center.longitude, radiusKm, bearing
            );

            if (point != null && point.isInAtlanta()) {
                route.add(point);
            }
        }

        // Close the loop
        if (!route.isEmpty()) {
            route.add(route.get(0));
        }

        return route;
    }

    /**
     * Generate route following major roads (simplified simulation)
     */
    public static List<GeoUtils.Coordinates> generateRoadRoute(GeoUtils.Coordinates start,
                                                               GeoUtils.Coordinates end) {
        List<GeoUtils.Coordinates> route = new ArrayList<>();
        route.add(start);

        // Find intermediate hotspot that's roughly on the way
        GeoUtils.Coordinates bestIntermediate = null;
        double bestScore = Double.MAX_VALUE;

        for (GeoUtils.Coordinates hotspot : ATLANTA_HOTSPOTS) {
            // Calculate total distance via this hotspot
            double dist1 = GeoUtils.calculateDistance(
                    start.latitude, start.longitude, hotspot.latitude, hotspot.longitude
            );
            double dist2 = GeoUtils.calculateDistance(
                    hotspot.latitude, hotspot.longitude, end.latitude, end.longitude
            );
            double totalDist = dist1 + dist2;

            // Calculate direct distance
            double directDist = GeoUtils.calculateDistance(
                    start.latitude, start.longitude, end.latitude, end.longitude
            );

            // Score based on how much detour this adds
            double score = totalDist - directDist;

            if (score < bestScore && score > 0) {
                bestScore = score;
                bestIntermediate = hotspot;
            }
        }

        // Add intermediate point if it's a reasonable detour
        if (bestIntermediate != null && bestScore < 5.0) { // Less than 5km detour
            route.add(bestIntermediate);
        }

        route.add(end);
        return route;
    }

    /**
     * Calculate route statistics
     */
    public static RouteStats calculateRouteStats(List<GeoUtils.Coordinates> route) {
        if (route.size() < 2) {
            return new RouteStats(0, 0, 0);
        }

        double totalDistance = 0;
        double totalTime = 0;
        int segmentCount = route.size() - 1;

        for (int i = 0; i < route.size() - 1; i++) {
            GeoUtils.Coordinates from = route.get(i);
            GeoUtils.Coordinates to = route.get(i + 1);

            double segmentDistance = GeoUtils.calculateDistance(
                    from.latitude, from.longitude, to.latitude, to.longitude
            );

            totalDistance += segmentDistance;
            totalTime += GeoUtils.calculateEstimatedTravelTime(segmentDistance);
        }

        return new RouteStats(totalDistance, totalTime, segmentCount);
    }

    /**
     * Route statistics class
     */
    public static class RouteStats {
        public final double totalDistanceKm;
        public final double totalTimeMinutes;
        public final int segmentCount;

        public RouteStats(double totalDistanceKm, double totalTimeMinutes, int segmentCount) {
            this.totalDistanceKm = totalDistanceKm;
            this.totalTimeMinutes = totalTimeMinutes;
            this.segmentCount = segmentCount;
        }

        @Override
        public String toString() {
            return String.format("Route: %.1f km, %.0f min, %d segments",
                    totalDistanceKm, totalTimeMinutes, segmentCount);
        }
    }
}