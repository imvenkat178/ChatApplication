package com.cabapp.repository;

import com.cabapp.model.Ride;
import com.cabapp.model.RideStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

    // Find rides by user ID
    List<Ride> findByUserId(String userId);

    // Find rides by driver ID
    List<Ride> findByDriverId(String driverId);

    // Find rides by status
    List<Ride> findByStatus(RideStatus status);

    // Find active rides (not completed or cancelled)
    @Query("SELECT r FROM Ride r WHERE r.status IN ('REQUESTED', 'ASSIGNED', 'DRIVER_ARRIVED', 'IN_PROGRESS')")
    List<Ride> findActiveRides();

    // Find pending rides (waiting for driver assignment)
    @Query("SELECT r FROM Ride r WHERE r.status = 'REQUESTED' ORDER BY r.rideRequestedAt ASC")
    List<Ride> findPendingRides();

    // Find rides within a time range (useful for analytics)
    @Query("SELECT r FROM Ride r WHERE r.rideRequestedAt BETWEEN :startTime AND :endTime")
    List<Ride> findRidesByTimeRange(@Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime);

    // Find rides within geographic bounding box (for admin dashboard)
    @Query("SELECT r FROM Ride r WHERE r.pickupLatitude BETWEEN :minLat AND :maxLat " +
            "AND r.pickupLongitude BETWEEN :minLon AND :maxLon")
    List<Ride> findRidesInBoundingBox(@Param("minLat") Double minLat,
                                      @Param("maxLat") Double maxLat,
                                      @Param("minLon") Double minLon,
                                      @Param("maxLon") Double maxLon);

    // Find rides near a specific location (using coordinates)
    @Query("SELECT r FROM Ride r WHERE " +
            "SQRT(POWER(r.pickupLatitude - :latitude, 2) + POWER(r.pickupLongitude - :longitude, 2)) <= :radius")
    List<Ride> findRidesNearLocation(@Param("latitude") Double latitude,
                                     @Param("longitude") Double longitude,
                                     @Param("radius") Double radius);

    // Count rides by status
    long countByStatus(RideStatus status);

    // Find user's completed rides
    @Query("SELECT r FROM Ride r WHERE r.userId = :userId AND r.status = 'COMPLETED' " +
            "ORDER BY r.rideCompletedAt DESC")
    List<Ride> findUserCompletedRides(@Param("userId") String userId);

    // Find driver's completed rides
    @Query("SELECT r FROM Ride r WHERE r.driverId = :driverId AND r.status = 'COMPLETED' " +
            "ORDER BY r.rideCompletedAt DESC")
    List<Ride> findDriverCompletedRides(@Param("driverId") String driverId);

    // Find current active ride for a user
    @Query("SELECT r FROM Ride r WHERE r.userId = :userId " +
            "AND r.status IN ('REQUESTED', 'ASSIGNED', 'DRIVER_ARRIVED', 'IN_PROGRESS') " +
            "ORDER BY r.rideRequestedAt DESC")
    Optional<Ride> findUserActiveRide(@Param("userId") String userId);

    // Find current active ride for a driver
    @Query("SELECT r FROM Ride r WHERE r.driverId = :driverId " +
            "AND r.status IN ('ASSIGNED', 'DRIVER_ARRIVED', 'IN_PROGRESS') " +
            "ORDER BY r.rideAcceptedAt DESC")
    Optional<Ride> findDriverActiveRide(@Param("driverId") String driverId);

    // Calculate average fare
    @Query("SELECT AVG(r.actualFare) FROM Ride r WHERE r.actualFare IS NOT NULL")
    Double calculateAverageFare();

    // Calculate average estimated fare (for rides not yet completed)
    @Query("SELECT AVG(r.estimatedFare) FROM Ride r WHERE r.estimatedFare IS NOT NULL")
    Double calculateAverageEstimatedFare();

    // Find rides scheduled for future
    @Query("SELECT r FROM Ride r WHERE r.scheduledRide = true AND r.scheduledTime > :currentTime")
    List<Ride> findScheduledRides(@Param("currentTime") LocalDateTime currentTime);

    // Find rides that took longer than expected (for analysis)
    @Query("SELECT r FROM Ride r WHERE r.rideStartedAt IS NOT NULL AND r.rideCompletedAt IS NOT NULL " +
            "AND TIMESTAMPDIFF(MINUTE, r.rideStartedAt, r.rideCompletedAt) > 60")
    List<Ride> findLongDurationRides();

    // Find rides by vehicle type
    @Query("SELECT r FROM Ride r WHERE r.vehicleType = :vehicleType")
    List<Ride> findByVehicleType(@Param("vehicleType") String vehicleType);

    // Find rides by payment method
    @Query("SELECT r FROM Ride r WHERE r.paymentMethod = :paymentMethod")
    List<Ride> findByPaymentMethod(@Param("paymentMethod") String paymentMethod);

    // Analytics: Get ride statistics by date range
    @Query("SELECT " +
            "COUNT(r) as totalRides, " +
            "AVG(r.actualFare) as avgFare, " +
            "AVG(r.distance) as avgDistance, " +
            "AVG(TIMESTAMPDIFF(MINUTE, r.rideRequestedAt, r.rideCompletedAt)) as avgDurationMinutes " +
            "FROM Ride r WHERE r.rideRequestedAt BETWEEN :startDate AND :endDate " +
            "AND r.status = 'COMPLETED'")
    Object[] getRideStatistics(@Param("startDate") LocalDateTime startDate,
                               @Param("endDate") LocalDateTime endDate);

    // Find rides with promo codes applied
    @Query("SELECT r FROM Ride r WHERE r.promoCode IS NOT NULL AND r.promoCode != ''")
    List<Ride> findRidesWithPromoCodes();

    // Find cancelled rides with reasons (you'd need to add cancellationReason field)
    @Query("SELECT r FROM Ride r WHERE r.status = 'CANCELLED' ORDER BY r.rideCancelledAt DESC")
    List<Ride> findCancelledRides();

    // Find rides that were auto-coordinated (have both pickup and dropoff coordinates)
    @Query("SELECT r FROM Ride r WHERE r.pickupLatitude IS NOT NULL AND r.pickupLongitude IS NOT NULL " +
            "AND r.dropoffLatitude IS NOT NULL AND r.dropoffLongitude IS NOT NULL")
    List<Ride> findRidesWithAutoCoordinates();
}