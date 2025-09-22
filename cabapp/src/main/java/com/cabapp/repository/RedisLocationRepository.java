package com.cabapp.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.geo.GeoResults;  // ✅ CORRECT
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Repository for Redis geospatial operations using direct Redis commands
 * Provides low-level access to Redis GEO commands for driver location tracking
 */
@Repository
@Slf4j
public class RedisLocationRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DRIVER_LOCATION_KEY = "driver_locations";
    private static final String DRIVER_STATUS_KEY = "driver_status:";

    public RedisLocationRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Add driver location using Redis GEOADD command
     * Command: GEOADD driver_locations longitude latitude driverId
     */
    public boolean addDriverLocation(String driverId, double latitude, double longitude) {
        try {
            Point point = new Point(longitude, latitude);
            Long result = redisTemplate.opsForGeo().add(DRIVER_LOCATION_KEY, point, driverId);

            // Also store driver status and timestamp
            String statusKey = DRIVER_STATUS_KEY + driverId;
            Map<String, Object> driverData = Map.of(
                    "latitude", latitude,
                    "longitude", longitude,
                    "lastUpdate", System.currentTimeMillis(),
                    "status", "AVAILABLE"
            );

            redisTemplate.opsForHash().putAll(statusKey, driverData);
            redisTemplate.expire(statusKey, java.time.Duration.ofMinutes(30)); // Expire after 30 minutes

            log.debug("GEOADD: Added driver {} at {}, {} to Redis", driverId, latitude, longitude);
            return result != null && result > 0;

        } catch (Exception e) {
            log.error("Error in GEOADD for driver {}: {}", driverId, e.getMessage());
            return false;
        }
    }

    /**
     * Find drivers within radius using Redis GEORADIUS command
     * Command: GEORADIUS driver_locations longitude latitude radius km WITHDIST WITHCOORD ASC COUNT 10
     */
    public List<DriverLocationResult> findDriversWithinRadius(double centerLat, double centerLng,
                                                              double radiusKm, int limit) {
        try {
            Point center = new Point(centerLng, centerLat);
            Distance radius = new Distance(radiusKm, Metrics.KILOMETERS);
            Circle searchArea = new Circle(center, radius);

            // Configure GEORADIUS options
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                    .newGeoRadiusArgs()
                    .includeDistance()      // WITHDIST
                    .includeCoordinates()   // WITHCOORD
                    .sortAscending()        // ASC (nearest first)
                    .limit(limit);          // COUNT limit

            GeoResults<RedisGeoCommands.GeoLocation<Object>> results = redisTemplate.opsForGeo()
                    .radius(DRIVER_LOCATION_KEY, searchArea, args);

            if (results == null) {
                return List.of();
            }

            return results.getContent().stream()
                    .map(this::convertToDriverLocationResult)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error in GEORADIUS: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get driver position using Redis GEOPOS command
     * Command: GEOPOS driver_locations driverId
     */
    public Point getDriverPosition(String driverId) {
        try {
            List<Point> positions = redisTemplate.opsForGeo().position(DRIVER_LOCATION_KEY, driverId);

            if (positions != null && !positions.isEmpty() && positions.get(0) != null) {
                Point position = positions.get(0);
                log.debug("GEOPOS: Driver {} at {}, {}", driverId, position.getY(), position.getX());
                return position;
            }

            return null;

        } catch (Exception e) {
            log.error("Error in GEOPOS for driver {}: {}", driverId, e.getMessage());
            return null;
        }
    }

    /**
     * Calculate distance between two drivers using Redis GEODIST command
     * Command: GEODIST driver_locations driverId1 driverId2 km
     */
    public Double getDistanceBetweenDrivers(String driverId1, String driverId2) {
        try {
            Distance distance = redisTemplate.opsForGeo()
                    .distance(DRIVER_LOCATION_KEY, driverId1, driverId2, Metrics.KILOMETERS);

            if (distance != null) {
                log.debug("GEODIST: Distance between {} and {}: {} km",
                        driverId1, driverId2, distance.getValue());
                return distance.getValue();
            }

            return null;

        } catch (Exception e) {
            log.error("Error in GEODIST between {} and {}: {}", driverId1, driverId2, e.getMessage());
            return null;
        }
    }

    /**
     * Get geohash for driver location using Redis GEOHASH command
     * Command: GEOHASH driver_locations driverId
     */
    public String getDriverGeoHash(String driverId) {
        try {
            List<String> hashes = redisTemplate.opsForGeo().hash(DRIVER_LOCATION_KEY, driverId);

            if (hashes != null && !hashes.isEmpty()) {
                String hash = hashes.get(0);
                log.debug("GEOHASH: Driver {} hash: {}", driverId, hash);
                return hash;
            }

            return null;

        } catch (Exception e) {
            log.error("Error in GEOHASH for driver {}: {}", driverId, e.getMessage());
            return null;
        }
    }

    /**
     * Remove driver from geospatial index using Redis ZREM command
     * Command: ZREM driver_locations driverId
     */
    public boolean removeDriver(String driverId) {
        try {
            Long removed = redisTemplate.opsForGeo().remove(DRIVER_LOCATION_KEY, driverId);

            // Also remove driver status
            String statusKey = DRIVER_STATUS_KEY + driverId;
            redisTemplate.delete(statusKey);

            boolean success = removed != null && removed > 0;
            log.debug("ZREM: Removed driver {} from Redis: {}", driverId, success);
            return success;

        } catch (Exception e) {
            log.error("Error in ZREM for driver {}: {}", driverId, e.getMessage());
            return false;
        }
    }

    /**
     * Get all drivers in geospatial index using Redis ZRANGE command
     * Command: ZRANGE driver_locations 0 -1
     */
    public List<String> getAllDriverIds() {
        try {
            return redisTemplate.opsForZSet()
                    .range(DRIVER_LOCATION_KEY, 0, -1)
                    .stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting all driver IDs: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Get count of drivers in geospatial index using Redis ZCARD command
     * Command: ZCARD driver_locations
     */
    public long getDriverCount() {
        try {
            Long count = redisTemplate.opsForZSet().zCard(DRIVER_LOCATION_KEY);
            return count != null ? count : 0;

        } catch (Exception e) {
            log.error("Error getting driver count: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Update driver status in Redis hash
     */
    public void updateDriverStatus(String driverId, String status) {
        try {
            String statusKey = DRIVER_STATUS_KEY + driverId;
            redisTemplate.opsForHash().put(statusKey, "status", status);
            redisTemplate.opsForHash().put(statusKey, "lastUpdate", System.currentTimeMillis());

            log.debug("Updated driver {} status to: {}", driverId, status);

        } catch (Exception e) {
            log.error("Error updating driver {} status: {}", driverId, e.getMessage());
        }
    }

    /**
     * Get driver status from Redis hash
     */
    public String getDriverStatus(String driverId) {
        try {
            String statusKey = DRIVER_STATUS_KEY + driverId;
            Object status = redisTemplate.opsForHash().get(statusKey, "status");
            return status != null ? status.toString() : null;

        } catch (Exception e) {
            log.error("Error getting driver {} status: {}", driverId, e.getMessage());
            return null;
        }
    }

    /**
     * Find drivers by status within radius (combining GEO and HASH operations)
     */
    public List<DriverLocationResult> findAvailableDriversWithinRadius(double centerLat, double centerLng,
                                                                       double radiusKm, int limit) {
        List<DriverLocationResult> nearbyDrivers = findDriversWithinRadius(centerLat, centerLng, radiusKm, limit * 2);

        // Filter by status
        return nearbyDrivers.stream()
                .filter(driver -> "AVAILABLE".equals(getDriverStatus(driver.driverId)))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Cleanup stale driver locations (older than specified minutes)
     */
    public int cleanupStaleLocations(int maxAgeMinutes) {
        try {
            List<String> allDrivers = getAllDriverIds();
            long cutoffTime = System.currentTimeMillis() - (maxAgeMinutes * 60 * 1000L);
            int removedCount = 0;

            for (String driverId : allDrivers) {
                String statusKey = DRIVER_STATUS_KEY + driverId;
                Object lastUpdateObj = redisTemplate.opsForHash().get(statusKey, "lastUpdate");

                if (lastUpdateObj != null) {
                    long lastUpdate = Long.parseLong(lastUpdateObj.toString());
                    if (lastUpdate < cutoffTime) {
                        removeDriver(driverId);
                        removedCount++;
                        log.debug("Removed stale driver location: {}", driverId);
                    }
                }
            }

            log.info("Cleaned up {} stale driver locations older than {} minutes", removedCount, maxAgeMinutes);
            return removedCount;

        } catch (Exception e) {
            log.error("Error during cleanup of stale locations: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Convert Redis GeoResult to DriverLocationResult
     */
    private DriverLocationResult convertToDriverLocationResult(GeoResult<RedisGeoCommands.GeoLocation<Object>> result) {
        RedisGeoCommands.GeoLocation<Object> location = result.getContent();
        String driverId = (String) location.getName();
        Point point = location.getPoint();
        Distance distance = result.getDistance();

        return new DriverLocationResult(
                driverId,
                point.getY(), // latitude
                point.getX(), // longitude
                distance != null ? distance.getValue() : 0.0,
                getDriverStatus(driverId)
        );
    }

    /**
     * Result class for driver location queries
     */
    public static class DriverLocationResult {
        public final String driverId;
        public final double latitude;
        public final double longitude;
        public final double distanceKm;
        public final String status;

        public DriverLocationResult(String driverId, double latitude, double longitude,
                                    double distanceKm, String status) {
            this.driverId = driverId;
            this.latitude = latitude;
            this.longitude = longitude;
            this.distanceKm = distanceKm;
            this.status = status;

        }

        @Override
        public String toString() {
            return String.format("Driver[id=%s, lat=%.6f, lng=%.6f, dist=%.2fkm, status=%s]",
                    driverId, latitude, longitude, distanceKm, status);
        }
    }
}