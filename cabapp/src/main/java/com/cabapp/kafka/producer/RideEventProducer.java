package com.cabapp.kafka.producer;

import com.cabapp.model.dto.RideEvent;
import com.cabapp.model.Ride;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class RideEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "ride-events";

    public RideEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    // Publish ride created event
    public void publishRideCreated(Ride ride) {
        try {
            RideEvent event = RideEvent.rideCreated(
                    ride.getId(),
                    ride.getUserId(),
                    ride.getPickupAddress(),
                    ride.getDropoffAddress(),
                    ride.getPickupLatitude(),
                    ride.getPickupLongitude(),
                    ride.getDropoffLatitude(),
                    ride.getDropoffLongitude(),
                    ride.getVehicleType().name(),
                    ride.getEstimatedFare()
            );

            publishEvent(event);
            log.info("Published ride created event for ride: {}", ride.getId());

        } catch (Exception e) {
            log.error("Error publishing ride created event for ride {}: {}", ride.getId(), e.getMessage());
        }
    }

    // Publish driver assigned event
    public void publishDriverAssigned(Long rideId, String userId, String driverId) {
        try {
            RideEvent event = RideEvent.driverAssigned(rideId, userId, driverId);
            publishEvent(event);
            log.info("Published driver assigned event: ride={}, driver={}", rideId, driverId);

        } catch (Exception e) {
            log.error("Error publishing driver assigned event for ride {}: {}", rideId, e.getMessage());
        }
    }

    // Publish driver arrived event
    public void publishDriverArrived(Long rideId, String userId, String driverId) {
        try {
            RideEvent event = RideEvent.builder()
                    .rideId(rideId)
                    .userId(userId)
                    .driverId(driverId)
                    .eventType(RideEvent.DRIVER_ARRIVED)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            publishEvent(event);
            log.info("Published driver arrived event: ride={}, driver={}", rideId, driverId);

        } catch (Exception e) {
            log.error("Error publishing driver arrived event for ride {}: {}", rideId, e.getMessage());
        }
    }

    // Publish ride started event
    public void publishRideStarted(Long rideId, String userId, String driverId) {
        try {
            RideEvent event = RideEvent.builder()
                    .rideId(rideId)
                    .userId(userId)
                    .driverId(driverId)
                    .eventType(RideEvent.RIDE_STARTED)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            publishEvent(event);
            log.info("Published ride started event: ride={}, driver={}", rideId, driverId);

        } catch (Exception e) {
            log.error("Error publishing ride started event for ride {}: {}", rideId, e.getMessage());
        }
    }

    // Publish ride completed event
    public void publishRideCompleted(Long rideId, String userId, String driverId, Double actualFare) {
        try {
            RideEvent event = RideEvent.rideCompleted(rideId, userId, driverId, actualFare);
            publishEvent(event);
            log.info("Published ride completed event: ride={}, driver={}, fare={}", rideId, driverId, actualFare);

        } catch (Exception e) {
            log.error("Error publishing ride completed event for ride {}: {}", rideId, e.getMessage());
        }
    }

    // Publish ride cancelled event
    public void publishRideCancelled(Long rideId, String userId, String driverId, String reason) {
        try {
            RideEvent event = RideEvent.builder()
                    .rideId(rideId)
                    .userId(userId)
                    .driverId(driverId)
                    .eventType(RideEvent.RIDE_CANCELLED)
                    .data(reason)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

            publishEvent(event);
            log.info("Published ride cancelled event: ride={}, driver={}, reason={}", rideId, driverId, reason);

        } catch (Exception e) {
            log.error("Error publishing ride cancelled event for ride {}: {}", rideId, e.getMessage());
        }
    }

    // Generic method to publish any ride event
    private void publishEvent(RideEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);

            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC,
                    event.getRideId().toString(), eventJson);

            future.whenComplete((result, failure) -> {
                if (failure == null) {
                    log.debug("Ride event sent: type={}, ride={}, offset={}",
                            event.getEventType(), event.getRideId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send ride event: type={}, ride={}, error={}",
                            event.getEventType(), event.getRideId(), failure.getMessage());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("Error serializing ride event: type={}, ride={}, error={}",
                    event.getEventType(), event.getRideId(), e.getMessage());
        } catch (Exception e) {
            log.error("Error publishing ride event: type={}, ride={}, error={}",
                    event.getEventType(), event.getRideId(), e.getMessage());
        }
    }
}