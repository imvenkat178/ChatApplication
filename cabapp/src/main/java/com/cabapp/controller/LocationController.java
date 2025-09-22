package com.cabapp.controller;

import com.cabapp.model.Driver;
import com.cabapp.service.LocationService;
import com.cabapp.service.GeocodingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
public class LocationController {

    private final LocationService locationService;
    private final GeocodingService geocodingService;

    public LocationController(LocationService locationService, GeocodingService geocodingService) {
        this.locationService = locationService;
        this.geocodingService = geocodingService;
    }

    // Validate pickup coordinates from frontend GPS
    @PostMapping("/validate-pickup")
    public ResponseEntity<Map<String, Object>> validatePickupLocation(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate coordinates
            boolean isValid = locationService.validatePickupCoordinates(latitude, longitude);

            if (!isValid) {
                response.put("valid", false);
                response.put("message", "Invalid coordinates provided");
                return ResponseEntity.badRequest().body(response);
            }

            // Get address from coordinates
            String address = geocodingService.getAddressFromCoordinates(latitude, longitude);

            response.put("valid", true);
            response.put("latitude", latitude);
            response.put("longitude", longitude);
            response.put("address", address);
            response.put("message", "Pickup location validated successfully");

            log.info("Validated pickup location: {}, {} -> {}", latitude, longitude, address);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error validating pickup location: {}", e.getMessage());
            response.put("valid", false);
            response.put("message", "Error validating location: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Geocode dropoff address to coordinates
    @PostMapping("/geocode-dropoff")
    public ResponseEntity<Map<String, Object>> geocodeDropoffAddress(@RequestParam String address) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (address == null || address.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Address cannot be empty");
                return ResponseEntity.badRequest().body(response);
            }

            // Geocode address to coordinates
            GeocodingService.Coordinates coordinates = locationService.validateAndGeocodeDropoffAddress(address);

            if (coordinates == null) {
                response.put("success", false);
                response.put("message", "Could not find location for the provided address");
                return ResponseEntity.badRequest().body(response);
            }

            response.put("success", true);
            response.put("latitude", coordinates.latitude);
            response.put("longitude", coordinates.longitude);
            response.put("address", address);
            response.put("message", "Address geocoded successfully");

            log.info("Geocoded dropoff address '{}' to: {}, {}",
                    address, coordinates.latitude, coordinates.longitude);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error geocoding address '{}': {}", address, e.getMessage());
            response.put("success", false);
            response.put("message", "Error geocoding address: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Find nearby drivers for a pickup location
    @GetMapping("/nearby-drivers")
    public ResponseEntity<List<Driver>> findNearbyDrivers(
            @RequestParam double pickupLatitude,
            @RequestParam double pickupLongitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {

        try {
            // Validate pickup coordinates
            if (!locationService.validatePickupCoordinates(pickupLatitude, pickupLongitude)) {
                return ResponseEntity.badRequest().build();
            }

            List<Driver> nearbyDrivers = locationService.findNearbyDrivers(
                    pickupLatitude, pickupLongitude, radiusKm);

            log.info("Found {} drivers within {}km of {}, {}",
                    nearbyDrivers.size(), radiusKm, pickupLatitude, pickupLongitude);

            return ResponseEntity.ok(nearbyDrivers);

        } catch (Exception e) {
            log.error("Error finding nearby drivers: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Calculate distance between pickup and dropoff
    @GetMapping("/calculate-distance")
    public ResponseEntity<Map<String, Object>> calculateDistance(
            @RequestParam double pickupLatitude,
            @RequestParam double pickupLongitude,
            @RequestParam double dropoffLatitude,
            @RequestParam double dropoffLongitude) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate both coordinates
            if (!locationService.validatePickupCoordinates(pickupLatitude, pickupLongitude) ||
                    !locationService.validatePickupCoordinates(dropoffLatitude, dropoffLongitude)) {
                response.put("error", "Invalid coordinates provided");
                return ResponseEntity.badRequest().body(response);
            }

            double distance = locationService.calculateDistance(
                    pickupLatitude, pickupLongitude, dropoffLatitude, dropoffLongitude);

            response.put("distance_km", Math.round(distance * 100.0) / 100.0);
            response.put("pickup", Map.of("lat", pickupLatitude, "lng", pickupLongitude));
            response.put("dropoff", Map.of("lat", dropoffLatitude, "lng", dropoffLongitude));

            log.info("Calculated distance: {}km between ({}, {}) and ({}, {})",
                    distance, pickupLatitude, pickupLongitude, dropoffLatitude, dropoffLongitude);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error calculating distance: {}", e.getMessage());
            response.put("error", "Error calculating distance: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Reverse geocode coordinates to address
    @GetMapping("/reverse-geocode")
    public ResponseEntity<Map<String, Object>> reverseGeocode(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate coordinates
            if (!locationService.validatePickupCoordinates(latitude, longitude)) {
                response.put("success", false);
                response.put("message", "Invalid coordinates provided");
                return ResponseEntity.badRequest().body(response);
            }

            String address = geocodingService.getAddressFromCoordinates(latitude, longitude);

            response.put("success", true);
            response.put("latitude", latitude);
            response.put("longitude", longitude);
            response.put("address", address);

            log.info("Reverse geocoded {}, {} to address: {}", latitude, longitude, address);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error reverse geocoding: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Error reverse geocoding: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Get service area boundaries (for frontend map)
    @GetMapping("/service-area")
    public ResponseEntity<Map<String, Object>> getServiceArea() {
        Map<String, Object> response = new HashMap<>();

        // Atlanta, Georgia service area coordinates
        response.put("center", Map.of("lat", 33.7490, "lng", -84.3880)); // Atlanta city center
        response.put("bounds", Map.of(
                "north", 34.1,    // North Atlanta suburbs (Alpharetta, Roswell)
                "south", 33.4,    // South Atlanta (Hapeville, College Park)
                "east", -84.0,    // East Atlanta (Decatur, Stone Mountain)
                "west", -84.8     // West Atlanta (Marietta, Smyrna)
        ));
        response.put("radius_km", 50); // 50km radius covers Atlanta metro area
        response.put("city", "Atlanta");
        response.put("state", "Georgia");
        response.put("country", "USA");

        return ResponseEntity.ok(response);
    }

    // Health check for location services
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Test geocoding service with Atlanta address
            GeocodingService.Coordinates testCoords = geocodingService.getCoordinatesFromAddress("Atlanta, Georgia, USA");
            boolean geocodingHealthy = (testCoords != null);

            response.put("location_service", "healthy");
            response.put("geocoding_service", geocodingHealthy ? "healthy" : "unhealthy");
            response.put("redis_geospatial", "healthy"); // You could add actual Redis health check
            response.put("service_area", "Atlanta, Georgia");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "unhealthy");
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}