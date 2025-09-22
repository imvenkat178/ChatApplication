package com.cabapp.repository;

import com.cabapp.model.Driver;
import com.cabapp.model.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, String> {

    // Find drivers by status
    List<Driver> findByStatus(DriverStatus status);

    // Find available drivers (for ride matching)
    @Query("SELECT d FROM Driver d WHERE d.status = 'AVAILABLE'")
    List<Driver> findAvailableDrivers();

    // Find drivers by list of IDs and status (used by LocationService)
    @Query("SELECT d FROM Driver d WHERE d.id IN :driverIds AND d.status = :status")
    List<Driver> findByIdInAndStatus(@Param("driverIds") List<String> driverIds,
                                     @Param("status") String status);

    // Find drivers within a geographic bounding box
    @Query("SELECT d FROM Driver d WHERE d.currentLatitude IS NOT NULL AND d.currentLongitude IS NOT NULL " +
            "AND d.currentLatitude BETWEEN :minLat AND :maxLat " +
            "AND d.currentLongitude BETWEEN :minLon AND :maxLon")
    List<Driver> findDriversInBoundingBox(@Param("minLat") Double minLat,
                                          @Param("maxLat") Double maxLat,
                                          @Param("minLon") Double minLon,
                                          @Param("maxLon") Double maxLon);

    // Find drivers near a specific location (using simple distance calculation)
    @Query("SELECT d FROM Driver d WHERE d.currentLatitude IS NOT NULL AND d.currentLongitude IS NOT NULL " +
            "AND d.status = 'AVAILABLE' " +
            "AND SQRT(POWER(d.currentLatitude - :latitude, 2) + POWER(d.currentLongitude - :longitude, 2)) <= :radius")
    List<Driver> findDriversNearLocation(@Param("latitude") Double latitude,
                                         @Param("longitude") Double longitude,
                                         @Param("radius") Double radius);

    // Find driver by current ride ID
    @Query("SELECT d FROM Driver d WHERE d.currentRideId = :rideId")
    Optional<Driver> findByCurrentRideId(@Param("rideId") Long rideId);

    // Find drivers who haven't updated location recently (for cleanup)
    @Query("SELECT d FROM Driver d WHERE d.lastLocationUpdate < :cutoffTime OR d.lastLocationUpdate IS NULL")
    List<Driver> findDriversWithStaleLocation(@Param("cutoffTime") LocalDateTime cutoffTime);

    // Find online drivers (not offline)
    @Query("SELECT d FROM Driver d WHERE d.status != 'OFFLINE'")
    List<Driver> findOnlineDrivers();

    // Find drivers by vehicle type
    @Query("SELECT d FROM Driver d WHERE d.vehicleType = :vehicleType AND d.status = 'AVAILABLE'")
    List<Driver> findAvailableDriversByVehicleType(@Param("vehicleType") String vehicleType);

    // Count drivers by status
    long countByStatus(DriverStatus status);

    // Find drivers with recent activity
    @Query("SELECT d FROM Driver d WHERE d.lastStatusUpdate > :sinceTime")
    List<Driver> findDriversWithRecentActivity(@Param("sinceTime") LocalDateTime sinceTime);

    // Find drivers currently on a ride
    @Query("SELECT d FROM Driver d WHERE d.status IN ('ASSIGNED', 'BUSY') AND d.currentRideId IS NOT NULL")
    List<Driver> findDriversOnRide();

    // Find drivers who are taking a break
    @Query("SELECT d FROM Driver d WHERE d.status = 'BREAK'")
    List<Driver> findDriversOnBreak();

    // Analytics: Get driver statistics
    @Query("SELECT " +
            "COUNT(d) as totalDrivers, " +
            "SUM(CASE WHEN d.status = 'AVAILABLE' THEN 1 ELSE 0 END) as availableDrivers, " +
            "SUM(CASE WHEN d.status = 'BUSY' THEN 1 ELSE 0 END) as busyDrivers, " +
            "SUM(CASE WHEN d.status = 'OFFLINE' THEN 1 ELSE 0 END) as offlineDrivers " +
            "FROM Driver d")
    Object[] getDriverStatistics();

    // Find drivers by registration date range
    @Query("SELECT d FROM Driver d WHERE d.createdAt BETWEEN :startDate AND :endDate")
    List<Driver> findDriversByRegistrationDateRange(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    // Find drivers with location data (have coordinates)
    @Query("SELECT d FROM Driver d WHERE d.currentLatitude IS NOT NULL AND d.currentLongitude IS NOT NULL")
    List<Driver> findDriversWithLocationData();

    // Find drivers without location data (need location update)
    @Query("SELECT d FROM Driver d WHERE d.currentLatitude IS NULL OR d.currentLongitude IS NULL")
    List<Driver> findDriversWithoutLocationData();

    // Find nearest available driver to a location (ordered by simple distance)
    @Query("SELECT d FROM Driver d WHERE d.currentLatitude IS NOT NULL AND d.currentLongitude IS NOT NULL " +
            "AND d.status = 'AVAILABLE' " +
            "ORDER BY SQRT(POWER(d.currentLatitude - :latitude, 2) + POWER(d.currentLongitude - :longitude, 2)) ASC")
    List<Driver> findNearestAvailableDrivers(@Param("latitude") Double latitude,
                                             @Param("longitude") Double longitude);

    // Find drivers by license plate (for admin lookup)
    @Query("SELECT d FROM Driver d WHERE d.licensePlate = :licensePlate")
    Optional<Driver> findByLicensePlate(@Param("licensePlate") String licensePlate);

    // Find drivers by phone number
    @Query("SELECT d FROM Driver d WHERE d.phoneNumber = :phoneNumber")
    Optional<Driver> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    // Find drivers who joined recently (last 30 days)
    @Query("SELECT d FROM Driver d WHERE d.createdAt > :thirtyDaysAgo")
    List<Driver> findRecentlyJoinedDrivers(@Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);

    // Find drivers in Atlanta service area (using coordinate bounds)
    @Query("SELECT d FROM Driver d WHERE d.currentLatitude IS NOT NULL AND d.currentLongitude IS NOT NULL " +
            "AND d.currentLatitude BETWEEN 33.4 AND 34.1 " +
            "AND d.currentLongitude BETWEEN -84.8 AND -84.0")
    List<Driver> findDriversInAtlantaServiceArea();
}