package com.cabapp.kafka.producer;

import com.cabapp.model.dto.DriverLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class LocationUpdateProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "driver-locations";

    public LocationUpdateProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // Publish driver location update
    public void publishLocationUpdate(String driverId, Double latitude, Double longitude, String status) {
        try {
            DriverLocation location = DriverLocation.builder()
                    .driverId(driverId)
                    .latitude(latitude)
                    .longitude(longitude)
                    .status(status)
                    .timestamp(LocalDateTime.now())
                    .build();

            publishLocationUpdate(location);

        } catch (Exception e) {
            log.error("Error creating location update for driver {}: {}", driverId, e.getMessage());
        }
    }

    // Publish driver location object
    public void publishLocationUpdate(DriverLocation location) {
        try {
            // Validate location data
            if (!location.isValidCoordinates()) {
                log.error("Invalid coordinates for driver {}: {}, {}",
                        location.getDriverId(), location.getLatitude(), location.getLongitude());
                return;
            }

            // Check if in Atlanta service area
            if (!location.isInAtlantaServiceArea()) {
                log.warn("Driver {} location outside Atlanta service area: {}, {}",
                        location.getDriverId(), location.getLatitude(), location.getLongitude());
                // Still publish but log warning
            }

            String locationJson = objectMapper.writeValueAsString(location);

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC,
                    location.getDriverId(), locationJson);

            future.whenComplete((result, failure) -> {
                if (failure == null) {
                    log.debug("Location update sent for driver {}: offset={}",
                            location.getDriverId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send location update for driver {}: {}",
                            location.getDriverId(), failure.getMessage());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("Error serializing location update for driver {}: {}",
                    location.getDriverId(), e.getMessage());
        } catch (Exception e) {
            log.error("Error publishing location update for driver {}: {}",
                    location.getDriverId(), e.getMessage());
        }
    }

    // Publish driver going offline
    public void publishDriverOffline(String driverId) {
        try {
            DriverLocation offlineLocation = DriverLocation.builder()
                    .driverId(driverId)
                    .status("OFFLINE")
                    .timestamp(LocalDateTime.now())
                    .build();

            String locationJson = objectMapper.writeValueAsString(offlineLocation);

            kafkaTemplate.send(TOPIC, driverId, locationJson);
            log.info("Published driver offline event for: {}", driverId);

        } catch (Exception e) {
            log.error("Error publishing driver offline event for {}: {}", driverId, e.getMessage());
        }
    }

    // Publish bulk location updates (for simulation)
    public void publishBulkLocationUpdates(java.util.List<DriverLocation> locations) {
        for (DriverLocation location : locations) {
            publishLocationUpdate(location);
        }
        log.info("Published {} bulk location updates", locations.size());
    }
}