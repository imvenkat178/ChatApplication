package com.cabapp.service;

import com.cabapp.model.Driver;
import com.cabapp.repository.DriverRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.geo.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LocationService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final DriverRepository driverRepository;
    private final GeocodingService geocodingService; // NEW: For coordinate conversion

    private static final String DRIVER_LOCATION_KEY = "driver_locations";

    public LocationService(RedisTemplate<String, Object> redisTemplate,
                           DriverRepository driverRepository,
                           GeocodingService geocodingService) {
        this.redisTemplate = redisTemplate;
        this.driverRepository = driverRepository;
        this.geocodingService = geocodingService;
    }

    // Store driver location in Redis Geospatial
    public void updateDriverLocation(String driverId, double latitude, double longitude) {
        GeoOperations<String, Object> geoOps = redisTemplate.opsForGeo();

        Point point = new Point(longitude, latitude); // Redis uses (longitude, latitude)
        geoOps.add(DRIVER_LOCATION_KEY, point, driverId);

        log.debug("Updated driver {} location to: {}, {}", driverId, latitude, longitude);
    }

    // Find nearby drivers using Redis Geospatial (for ride matching)
    public List<Driver> findNearbyDrivers(double pickupLatitude, double pickupLongitude, double radiusKm) {
        GeoOperations<String, Object> geoOps = redisTemplate.opsForGeo();

        // Create search criteria
        Point center = new Point(pickupLongitude, pickupLatitude);
        Distance radius = new Distance(radiusKm, Metrics.KILOMETERS);
        Circle searchArea = new Circle(center, radius);

        // Search for nearby drivers
        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = geoOps.radius(DRIVER_LOCATION_KEY, searchArea);

        // Extract driver IDs and fetch from database
        List<String> driverIds = results.getContent().stream()
                .map(result -> (String) result.getContent().getName())
                .collect(Collectors.toList());

        List<Driver> nearbyDrivers = driverRepository.findByIdInAndStatus(driverIds, "AVAILABLE");

        log.info("Found {} available drivers within {}km of pickup location {}, {}",
                nearbyDrivers.size(), radiusKm, pickupLatitude, pickupLongitude);

        return nearbyDrivers;
    }

    // Get driver's current location from Redis
    public Point getDriverLocation(String driverId) {
        GeoOperations<String, Object> geoOps = redisTemplate.opsForGeo();

        List<Point> positions = geoOps.position(DRIVER_LOCATION_KEY, driverId);

        if (positions != null && !positions.isEmpty() && positions.get(0) != null) {
            Point location = positions.get(0);
            log.debug("Driver {} location: {}, {}", driverId, location.getY(), location.getX());
            return location;
        }

        log.warn("No location found for driver: {}", driverId);
        return null;
    }

    // Calculate distance between two points
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        GeoOperations<String, Object> geoOps = redisTemplate.opsForGeo();

        Point point1 = new Point(lon1, lat1);
        Point point2 = new Point(lon2, lat2);

        // Add temporary points for distance calculation
        String tempKey = "temp_distance_calc";
        geoOps.add(tempKey, point1, "point1");
        geoOps.add(tempKey, point2, "point2");

        Distance distance = geoOps.distance(tempKey, "point1", "point2", Metrics.KILOMETERS);

        // Clean up temporary data
        redisTemplate.delete(tempKey);

        return distance != null ? distance.getValue() : 0.0;
    }

    // NEW: Validate pickup coordinates from GPS
    public boolean validatePickupCoordinates(double latitude, double longitude) {
        // Basic coordinate validation
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            log.error("Invalid coordinates: lat={}, lon={}", latitude, longitude);
            return false;
        }

        // You could add more validation like:
        // - Check if coordinates are in your service area
        // - Verify coordinates are not in water/restricted areas

        log.debug("Pickup coordinates validated: {}, {}", latitude, longitude);
        return true;
    }

    // NEW: Validate dropoff address and get coordinates
    public GeocodingService.Coordinates validateAndGeocodeDropoffAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            log.error("Dropoff address is empty");
            return null;
        }

        // Use geocoding service to convert address to coordinates
        GeocodingService.Coordinates coordinates = geocodingService.getCoordinatesFromAddress(address);

        if (coordinates == null) {
            log.error("Could not geocode dropoff address: {}", address);
            return null;
        }

        log.info("Dropoff address '{}' geocoded to: {}, {}",
                address, coordinates.latitude, coordinates.longitude);

        return coordinates;
    }

    // Remove driver from location tracking (when going offline)
    public void removeDriverLocation(String driverId) {
        GeoOperations<String, Object> geoOps = redisTemplate.opsForGeo();
        geoOps.remove(DRIVER_LOCATION_KEY, driverId);

        log.info("Removed driver {} from location tracking", driverId);
    }

    // Get all drivers within a bounding box (for admin dashboard)
    public List<Driver> getDriversInArea(double minLat, double maxLat, double minLon, double maxLon) {
        // This would require custom implementation based on your needs
        // For now, we'll use a center point and radius approach

        double centerLat = (minLat + maxLat) / 2;
        double centerLon = (minLon + maxLon) / 2;
        double radius = calculateDistance(minLat, minLon, maxLat, maxLon) / 2;

        return findNearbyDrivers(centerLat, centerLon, radius);
    }
}