package com.cabapp.controller;

import com.cabapp.model.dto.RideRequestDTO;
import com.cabapp.model.Ride;
import com.cabapp.service.RideService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rides")
@Slf4j
@CrossOrigin(origins = "http://localhost:3000") // For React frontend
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    // Book a new ride with AUTO-COORDINATES
    @PostMapping
    public ResponseEntity<Ride> createRide(@Valid @RequestBody RideRequestDTO request) {
        try {
            log.info("Creating ride request for user: {} with auto-coordinates", request.getUserId());
            log.info("Pickup coordinates: {}, {}", request.getPickupLatitude(), request.getPickupLongitude());
            log.info("Dropoff address: {}", request.getDropoffAddress());

            Ride ride = rideService.createRideRequest(request);

            log.info("Ride created with ID: {} at {}", ride.getId(), ride.getRideRequestedAt());
            return ResponseEntity.ok(ride);
        } catch (Exception e) {
            log.error("Error creating ride: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Get ride details
    @GetMapping("/{rideId}")
    public ResponseEntity<Ride> getRide(@PathVariable Long rideId) {
        try {
            // This would need to be implemented in RideService
            return ResponseEntity.ok().build(); // Placeholder
        } catch (Exception e) {
            log.error("Error getting ride {}: {}", rideId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // Driver accepts ride - AUTO timestamp
    @PostMapping("/{rideId}/accept")
    public ResponseEntity<Ride> acceptRide(
            @PathVariable Long rideId,
            @RequestParam String driverId) {
        try {
            Ride ride = rideService.acceptRide(rideId, driverId);
            log.info("Ride {} accepted at {}", rideId, ride.getRideAcceptedAt());
            return ResponseEntity.ok(ride);
        } catch (Exception e) {
            log.error("Error accepting ride {}: {}", rideId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Driver arrived at pickup - AUTO timestamp
    @PostMapping("/{rideId}/arrived")
    public ResponseEntity<Ride> markDriverArrived(@PathVariable Long rideId) {
        try {
            Ride ride = rideService.markDriverArrived(rideId);
            log.info("Driver arrived for ride {} at {}", rideId, ride.getDriverArrivedAt());
            return ResponseEntity.ok(ride);
        } catch (Exception e) {
            log.error("Error marking driver arrived for ride {}: {}", rideId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Start ride - AUTO timestamp
    @PostMapping("/{rideId}/start")
    public ResponseEntity<Ride> startRide(@PathVariable Long rideId) {
        try {
            Ride ride = rideService.startRide(rideId);
            log.info("Ride {} started at {}", rideId, ride.getRideStartedAt());
            return ResponseEntity.ok(ride);
        } catch (Exception e) {
            log.error("Error starting ride {}: {}", rideId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Complete ride - AUTO timestamp
    @PostMapping("/{rideId}/complete")
    public ResponseEntity<Ride> completeRide(
            @PathVariable Long rideId,
            @RequestParam Double actualFare) {
        try {
            Ride ride = rideService.completeRide(rideId, actualFare);
            log.info("Ride {} completed at {}", rideId, ride.getRideCompletedAt());
            return ResponseEntity.ok(ride);
        } catch (Exception e) {
            log.error("Error completing ride {}: {}", rideId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Cancel ride - AUTO timestamp
    @PostMapping("/{rideId}/cancel")
    public ResponseEntity<Ride> cancelRide(@PathVariable Long rideId) {
        try {
            Ride ride = rideService.cancelRide(rideId);
            log.info("Ride {} cancelled at {}", rideId, ride.getRideCancelledAt());
            return ResponseEntity.ok(ride);
        } catch (Exception e) {
            log.error("Error cancelling ride {}: {}", rideId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}