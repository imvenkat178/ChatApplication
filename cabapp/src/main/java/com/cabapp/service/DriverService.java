package com.cabapp.service;

import com.cabapp.model.Driver;
import com.cabapp.model.DriverStatus;
import com.cabapp.repository.DriverRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class DriverService {

    private final DriverRepository driverRepository;
    private final LocationService locationService;
    private final NotificationService notificationService;

    public DriverService(DriverRepository driverRepository,
                         LocationService locationService,
                         NotificationService notificationService) {
        this.driverRepository = driverRepository;
        this.locationService = locationService;
        this.notificationService = notificationService;
    }

    // Find driver by ID
    public Driver findById(String driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + driverId));
    }

    // Update driver status
    @Transactional
    public Driver updateDriverStatus(String driverId, DriverStatus status) {
        Driver driver = findById(driverId);
        DriverStatus oldStatus = driver.getStatus();

        driver.setStatus(status);
        driver.setLastStatusUpdate(LocalDateTime.now());

        // Handle location tracking based on status
        if (status == DriverStatus.OFFLINE) {
            // Remove from location tracking when going offline
            locationService.removeDriverLocation(driverId);
            log.info("Driver {} went offline, removed from location tracking", driverId);
        } else if (oldStatus == DriverStatus.OFFLINE && status == DriverStatus.AVAILABLE) {
            // Driver came online - they should send location update soon
            log.info("Driver {} came online, waiting for location update", driverId);
        }

        Driver savedDriver = driverRepository.save(driver);

        log.info("Driver {} status updated from {} to {}",
                driverId, oldStatus, status);

        return savedDriver;
    }

    // Update driver location (called from location updates)
    @Transactional
    public void updateDriverLocation(String driverId, double latitude, double longitude) {
        // Validate coordinates
        if (!locationService.validatePickupCoordinates(latitude, longitude)) {
            throw new RuntimeException("Invalid coordinates for driver: " + driverId);
        }

        Driver driver = findById(driverId);

        // Only update location if driver is online
        if (driver.getStatus() == DriverStatus.OFFLINE) {
            log.warn("Received location update for offline driver: {}", driverId);
            return;
        }

        // Update in database
        driver.setCurrentLatitude(latitude);
        driver.setCurrentLongitude(longitude);
        driver.setLastLocationUpdate(LocalDateTime.now());
        driverRepository.save(driver);

        // Update in Redis for geospatial queries
        locationService.updateDriverLocation(driverId, latitude, longitude);

        log.debug("Updated location for driver {}: {}, {}",
                driverId, latitude, longitude);
    }

    // Find available drivers near a location (for ride matching)
    public List<Driver> findAvailableDriversNear(double pickupLatitude, double pickupLongitude, double radiusKm) {
        // Validate pickup coordinates
        if (!locationService.validatePickupCoordinates(pickupLatitude, pickupLongitude)) {
            throw new RuntimeException("Invalid pickup coordinates");
        }

        List<Driver> nearbyDrivers = locationService.findNearbyDrivers(
                pickupLatitude, pickupLongitude, radiusKm);

        // Filter for only available drivers (additional safety check)
        List<Driver> availableDrivers = nearbyDrivers.stream()
                .filter(driver -> driver.getStatus() == DriverStatus.AVAILABLE)
                .toList();

        log.info("Found {} available drivers within {}km of location {}, {}",
                availableDrivers.size(), radiusKm, pickupLatitude, pickupLongitude);

        return availableDrivers;
    }

    // Assign driver to ride
    @Transactional
    public Driver assignToRide(String driverId, Long rideId) {
        Driver driver = findById(driverId);

        if (driver.getStatus() != DriverStatus.AVAILABLE) {
            throw new RuntimeException("Driver " + driverId + " is not available for assignment");
        }

        // Update driver status
        driver.setStatus(DriverStatus.ASSIGNED);
        driver.setCurrentRideId(rideId);
        driver.setLastStatusUpdate(LocalDateTime.now());

        Driver savedDriver = driverRepository.save(driver);

        // Notify driver about ride assignment
        notificationService.notifyDriverAboutRideAssignment(driverId, rideId);

        log.info("Driver {} assigned to ride {}", driverId, rideId);

        return savedDriver;
    }

    // Mark driver as busy (ride started)
    @Transactional
    public Driver markBusy(String driverId) {
        Driver driver = findById(driverId);

        driver.setStatus(DriverStatus.BUSY);
        driver.setLastStatusUpdate(LocalDateTime.now());

        Driver savedDriver = driverRepository.save(driver);

        log.info("Driver {} marked as busy", driverId);

        return savedDriver;
    }

    // Complete ride and make driver available
    @Transactional
    public Driver completeRide(String driverId) {
        Driver driver = findById(driverId);

        driver.setStatus(DriverStatus.AVAILABLE);
        driver.setCurrentRideId(null);
        driver.setLastStatusUpdate(LocalDateTime.now());

        Driver savedDriver = driverRepository.save(driver);

        log.info("Driver {} completed ride and is now available", driverId);

        return savedDriver;
    }

    // Get driver's current location
    public String getDriverLocationInfo(String driverId) {
        Driver driver = findById(driverId);

        if (driver.getCurrentLatitude() != null && driver.getCurrentLongitude() != null) {
            return String.format("Driver %s location: %.6f, %.6f (updated: %s)",
                    driverId,
                    driver.getCurrentLatitude(),
                    driver.getCurrentLongitude(),
                    driver.getLastLocationUpdate());
        }

        return "No location available for driver: " + driverId;
    }

    // Get all available drivers
    public List<Driver> findAllAvailable() {
        return driverRepository.findByStatus(DriverStatus.AVAILABLE);
    }

    // Get all drivers with their status
    public List<Driver> findAll() {
        return driverRepository.findAll();
    }
}