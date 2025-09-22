package com.cabapp.kafka.consumer;

import com.cabapp.model.dto.DriverLocation;
import com.cabapp.service.DriverService;
import com.cabapp.service.LocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class LocationUpdateConsumer {

    private final DriverService driverService;
    private final LocationService locationService;
    private final ObjectMapper objectMapper;

    public LocationUpdateConsumer(DriverService driverService,
                                  LocationService locationService,
                                  ObjectMapper objectMapper) {
        this.driverService = driverService;
        this.locationService = locationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "driver-locations", groupId = "location-service")
    public void processLocationUpdate(String locationJson) {
        try {
            DriverLocation location = objectMapper.readValue(locationJson, DriverLocation.class);

            // Validate coordinates before processing
            if (!locationService.validatePickupCoordinates(location.getLatitude(), location.getLongitude())) {
                log.error("Invalid coordinates received for driver {}: {}, {}",
                        location.getDriverId(), location.getLatitude(), location.getLongitude());
                return;
            }

            // Update driver location in both database and Redis
            driverService.updateDriverLocation(
                    location.getDriverId(),
                    location.getLatitude(),
                    location.getLongitude()
            );

            log.debug("Processed location update for driver {}: {}, {}",
                    location.getDriverId(), location.getLatitude(), location.getLongitude());

        } catch (Exception e) {
            log.error("Error processing location update: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "driver-status-updates", groupId = "location-service")
    public void processDriverStatusUpdate(String statusUpdateJson) {
        try {
            // Process driver status changes that affect location tracking
            log.debug("Processing driver status update: {}", statusUpdateJson);

            // Parse status update and handle location tracking accordingly
            // If driver goes offline, location will be removed by DriverService

        } catch (Exception e) {
            log.error("Error processing driver status update: {}", e.getMessage(), e);
        }
    }
}