package com.cabapp.service;

import com.cabapp.model.Driver;
import com.cabapp.model.DriverStatus;
import com.cabapp.model.VehicleType;
import com.cabapp.repository.DriverRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@Slf4j
public class DriverSimulationService {

    private final DriverRepository driverRepository;
    private final DriverService driverService;
    private final LocationService locationService;

    private final Random random = new Random();
    private final List<String> simulatedDriverIds = new ArrayList<>();

    // Atlanta coordinates bounds for simulation
    private static final double ATLANTA_MIN_LAT = 33.4;
    private static final double ATLANTA_MAX_LAT = 34.1;
    private static final double ATLANTA_MIN_LNG = -84.8;
    private static final double ATLANTA_MAX_LNG = -84.0;

    // Movement parameters
    private static final double MAX_MOVE_DISTANCE = 0.001; // ~100 meters per update
    private static final int SIMULATION_INTERVAL_MS = 5000; // 5 seconds

    public DriverSimulationService(DriverRepository driverRepository,
                                   DriverService driverService,
                                   LocationService locationService) {
        this.driverRepository = driverRepository;
        this.driverService = driverService;
        this.locationService = locationService;
    }

    @PostConstruct
    public void initializeSimulation() {
        createSimulatedDrivers();
        log.info("Driver simulation initialized with {} drivers", simulatedDriverIds.size());
    }

    // Create simulated drivers in Atlanta area
    private void createSimulatedDrivers() {
        String[] driverNames = {
                "John Smith", "Mike Johnson", "David Wilson", "Chris Brown", "Alex Davis",
                "Ryan Miller", "Kevin Garcia", "Brian Martinez", "Jason Anderson", "Daniel Taylor"
        };

        VehicleType[] vehicleTypes = VehicleType.values();
        String[] vehicleModels = {
                "Toyota Camry", "Honda Accord", "Nissan Altima", "Ford Fusion", "Chevrolet Malibu",
                "Hyundai Elantra", "Kia Optima", "Volkswagen Jetta", "Subaru Legacy", "Mazda 6"
        };

        for (int i = 0; i < driverNames.length; i++) {
            try {
                String driverId = "sim_driver_" + UUID.randomUUID().toString().substring(0, 8);

                Driver driver = Driver.builder()
                        .id(driverId)
                        .name(driverNames[i])
                        .email(driverId + "@simulation.com")
                        .phoneNumber("+1555000" + String.format("%04d", i + 1))
                        .licensePlate("SIM" + String.format("%03d", i + 1))
                        .licenseNumber("DL" + String.format("%08d", i + 1))
                        .status(DriverStatus.AVAILABLE)
                        .vehicleType(vehicleTypes[i % vehicleTypes.length])
                        .vehicleModel(vehicleModels[i % vehicleModels.length])
                        .vehicleColor(getRandomColor())
                        .vehicleYear(2018 + random.nextInt(6)) // 2018-2023
                        .rating(4.0 + random.nextDouble()) // 4.0-5.0
                        .totalRides(random.nextInt(500) + 100) // 100-600 rides
                        .currentLatitude(generateRandomLatitude())
                        .currentLongitude(generateRandomLongitude())
                        .lastLocationUpdate(LocalDateTime.now())
                        .lastStatusUpdate(LocalDateTime.now())
                        .build();

                driverRepository.save(driver);
                simulatedDriverIds.add(driverId);

                // Add to location tracking
                locationService.updateDriverLocation(driverId,
                        driver.getCurrentLatitude(), driver.getCurrentLongitude());

                log.debug("Created simulated driver: {} at {}, {}",
                        driver.getName(), driver.getCurrentLatitude(), driver.getCurrentLongitude());

            } catch (Exception e) {
                log.error("Error creating simulated driver {}: {}", driverNames[i], e.getMessage());
            }
        }
    }

    // Update driver locations every 5 seconds
    @Scheduled(fixedRate = SIMULATION_INTERVAL_MS)
    public void updateSimulatedDriverLocations() {
        for (String driverId : simulatedDriverIds) {
            try {
                Driver driver = driverRepository.findById(driverId).orElse(null);
                if (driver != null && driver.getStatus() != DriverStatus.OFFLINE) {

                    // Move driver to new location
                    double newLat = driver.getCurrentLatitude() +
                            (random.nextDouble() - 0.5) * MAX_MOVE_DISTANCE * 2;
                    double newLng = driver.getCurrentLongitude() +
                            (random.nextDouble() - 0.5) * MAX_MOVE_DISTANCE * 2;

                    // Keep within Atlanta bounds
                    newLat = Math.max(ATLANTA_MIN_LAT, Math.min(ATLANTA_MAX_LAT, newLat));
                    newLng = Math.max(ATLANTA_MIN_LNG, Math.min(ATLANTA_MAX_LNG, newLng));

                    // Update location
                    driverService.updateDriverLocation(driverId, newLat, newLng);

                    // Randomly change status (simulate real driver behavior)
                    simulateStatusChange(driver);
                }

            } catch (Exception e) {
                log.error("Error updating simulated driver {}: {}", driverId, e.getMessage());
            }
        }

        log.debug("Updated locations for {} simulated drivers", simulatedDriverIds.size());
    }

    // Simulate driver status changes
    private void simulateStatusChange(Driver driver) {
        // 95% chance to stay in current status
        if (random.nextDouble() < 0.95) return;

        DriverStatus currentStatus = driver.getStatus();
        DriverStatus newStatus = currentStatus;

        switch (currentStatus) {
            case AVAILABLE:
                // 2% chance to go on break, 1% chance to go offline
                if (random.nextDouble() < 0.02) {
                    newStatus = DriverStatus.BREAK;
                } else if (random.nextDouble() < 0.01) {
                    newStatus = DriverStatus.OFFLINE;
                }
                break;

            case BREAK:
                // 10% chance to become available
                if (random.nextDouble() < 0.10) {
                    newStatus = DriverStatus.AVAILABLE;
                }
                break;

            case OFFLINE:
                // 5% chance to come online
                if (random.nextDouble() < 0.05) {
                    newStatus = DriverStatus.AVAILABLE;
                }
                break;

            default:
                // Don't change status if ASSIGNED or BUSY (on ride)
                break;
        }

        if (newStatus != currentStatus) {
            try {
                driverService.updateDriverStatus(driver.getId(), newStatus);
                log.debug("Simulated driver {} status changed from {} to {}",
                        driver.getId(), currentStatus, newStatus);
            } catch (Exception e) {
                log.error("Error updating driver status: {}", e.getMessage());
            }
        }
    }

    // Generate random coordinates within Atlanta area
    private double generateRandomLatitude() {
        return ATLANTA_MIN_LAT + random.nextDouble() * (ATLANTA_MAX_LAT - ATLANTA_MIN_LAT);
    }

    private double generateRandomLongitude() {
        return ATLANTA_MIN_LNG + random.nextDouble() * (ATLANTA_MAX_LNG - ATLANTA_MIN_LNG);
    }

    private String getRandomColor() {
        String[] colors = {"White", "Black", "Silver", "Gray", "Red", "Blue", "Green", "Yellow"};
        return colors[random.nextInt(colors.length)];
    }

    // Get simulation statistics
    public SimulationStats getSimulationStats() {
        long availableCount = simulatedDriverIds.stream()
                .mapToLong(id -> driverRepository.findById(id)
                        .map(driver -> driver.getStatus() == DriverStatus.AVAILABLE ? 1L : 0L)
                        .orElse(0L))
                .sum();

        return new SimulationStats(
                simulatedDriverIds.size(),
                (int) availableCount,
                simulatedDriverIds.size() - (int) availableCount
        );
    }

    // Stop simulation (for testing or shutdown)
    public void stopSimulation() {
        for (String driverId : simulatedDriverIds) {
            try {
                driverService.updateDriverStatus(driverId, DriverStatus.OFFLINE);
                locationService.removeDriverLocation(driverId);
            } catch (Exception e) {
                log.error("Error stopping simulation for driver {}: {}", driverId, e.getMessage());
            }
        }
        log.info("Driver simulation stopped");
    }

    // Inner class for simulation statistics
    public static class SimulationStats {
        public final int totalDrivers;
        public final int availableDrivers;
        public final int busyDrivers;

        public SimulationStats(int total, int available, int busy) {
            this.totalDrivers = total;
            this.availableDrivers = available;
            this.busyDrivers = busy;
        }
    }
}