package com.cabapp.kafka.consumer;

import com.cabapp.model.dto.RideEvent;
import com.cabapp.model.Ride;
import com.cabapp.model.RideStatus;
import com.cabapp.service.RideService;
import com.cabapp.service.DriverService;
import com.cabapp.service.NotificationService;
import com.cabapp.repository.RideRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class RideEventConsumer {

    private final RideService rideService;
    private final DriverService driverService;
    private final NotificationService notificationService;
    private final RideRepository rideRepository;
    private final ObjectMapper objectMapper;

    public RideEventConsumer(RideService rideService,
                             DriverService driverService,
                             NotificationService notificationService,
                             RideRepository rideRepository,
                             ObjectMapper objectMapper) {
        this.rideService = rideService;
        this.driverService = driverService;
        this.notificationService = notificationService;
        this.rideRepository = rideRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "ride-events", groupId = "ride-service")
    public void processRideEvent(String rideEventJson) {
        try {
            RideEvent event = objectMapper.readValue(rideEventJson, RideEvent.class);

            log.info("Processing ride event: {} for ride {}", event.getEventType(), event.getRideId());

            switch (event.getEventType()) {
                case "RIDE_CREATED":
                    handleRideCreated(event);
                    break;
                case "DRIVER_ASSIGNED":
                    handleDriverAssigned(event);
                    break;
                case "DRIVER_ARRIVED":
                    handleDriverArrived(event);
                    break;
                case "RIDE_STARTED":
                    handleRideStarted(event);
                    break;
                case "RIDE_COMPLETED":
                    handleRideCompleted(event);
                    break;
                case "RIDE_CANCELLED":
                    handleRideCancelled(event);
                    break;
                default:
                    log.warn("Unknown ride event type: {}", event.getEventType());
            }

        } catch (Exception e) {
            log.error("Error processing ride event: {}", e.getMessage(), e);
        }
    }

    private void handleRideCreated(RideEvent event) {
        Ride ride = rideRepository.findById(event.getRideId()).orElse(null);
        if (ride != null) {
            // Notify user about successful ride creation with auto-coordinates
            notificationService.notifyRideCreated(ride.getUserId(), ride);

            log.info("Ride {} created with auto-coordinates - Pickup: {}, Dropoff: {}",
                    ride.getId(), ride.getPickupAddress(), ride.getDropoffAddress());
        }
    }

    private void handleDriverAssigned(RideEvent event) {
        Ride ride = rideRepository.findById(event.getRideId()).orElse(null);
        if (ride != null && ride.getDriverId() != null) {
            // Update driver status
            driverService.assignToRide(ride.getDriverId(), ride.getId());

            // Notify user about driver assignment
            notificationService.notifyDriverAssigned(ride.getUserId(), ride.getDriverId(), ride);

            log.info("Driver {} assigned to ride {} with pickup at {}",
                    ride.getDriverId(), ride.getId(), ride.getPickupAddress());
        }
    }

    private void handleDriverArrived(RideEvent event) {
        Ride ride = rideRepository.findById(event.getRideId()).orElse(null);
        if (ride != null && ride.getDriverId() != null) {
            // Notify user that driver arrived at auto-detected pickup location
            notificationService.notifyDriverArrived(ride.getUserId(), ride.getDriverId(), ride);

            log.info("Driver {} arrived at pickup location {} for ride {}",
                    ride.getDriverId(), ride.getPickupAddress(), ride.getId());
        }
    }

    private void handleRideStarted(RideEvent event) {
        Ride ride = rideRepository.findById(event.getRideId()).orElse(null);
        if (ride != null && ride.getDriverId() != null) {
            // Update driver status to busy
            driverService.markBusy(ride.getDriverId());

            // Notify user about ride start with destination info
            notificationService.notifyRideStarted(ride.getUserId(), ride.getDriverId(), ride);

            log.info("Ride {} started - Going from {} to {}",
                    ride.getId(), ride.getPickupAddress(), ride.getDropoffAddress());
        }
    }

    private void handleRideCompleted(RideEvent event) {
        Ride ride = rideRepository.findById(event.getRideId()).orElse(null);
        if (ride != null && ride.getDriverId() != null) {
            // Make driver available again
            driverService.completeRide(ride.getDriverId());

            // Notify user about ride completion at auto-geocoded destination
            notificationService.notifyRideCompleted(ride.getUserId(), ride.getDriverId(), ride);

            log.info("Ride {} completed - Arrived at {} with fare ₹{}",
                    ride.getId(),
                    ride.getDropoffAddress(),
                    ride.getActualFare() != null ? ride.getActualFare() : ride.getEstimatedFare());
        }
    }

    private void handleRideCancelled(RideEvent event) {
        Ride ride = rideRepository.findById(event.getRideId()).orElse(null);
        if (ride != null) {
            // If driver was assigned, make them available
            if (ride.getDriverId() != null) {
                driverService.completeRide(ride.getDriverId());
            }

            // Notify user about cancellation
            notificationService.notifyRideCancelled(ride.getUserId(), ride, "Ride was cancelled");

            log.info("Ride {} cancelled - Route was {} to {}",
                    ride.getId(), ride.getPickupAddress(), ride.getDropoffAddress());
        }
    }
}