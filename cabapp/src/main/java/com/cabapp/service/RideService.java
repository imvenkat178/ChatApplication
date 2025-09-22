package com.cabapp.service;

import com.cabapp.model.dto.RideRequestDTO;
import com.cabapp.model.Ride;
import com.cabapp.model.PaymentMethod;
import com.cabapp.model.RideStatus;
import com.cabapp.repository.RideRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RideService {

    private final GeocodingService geocodingService;
    private final RideRepository rideRepository;

    public RideService(GeocodingService geocodingService, RideRepository rideRepository) {
        this.geocodingService = geocodingService;
        this.rideRepository = rideRepository;
    }

    public Ride createRideRequest(RideRequestDTO request) {
        validateRideRequest(request);

        if (request.getPickupLatitude() == null || request.getPickupLongitude() == null) {
            throw new RuntimeException("Pickup location not available. Please enable location access.");
        }

        if (request.getPickupAddress() == null) {
            String pickupAddress = geocodingService.getAddressFromCoordinates(
                    request.getPickupLatitude(), request.getPickupLongitude()
            );
            request.setPickupAddress(pickupAddress);
        }

        GeocodingService.Coordinates dropoffCoords = geocodingService
                .getCoordinatesFromAddress(request.getDropoffAddress());

        if (dropoffCoords == null) {
            throw new RuntimeException("Invalid dropoff address. Please provide a valid address.");
        }

        request.setDropoffLatitude(dropoffCoords.latitude);
        request.setDropoffLongitude(dropoffCoords.longitude);

        if (request.getPaymentMethod() == null) {
            request.setPaymentMethod(PaymentMethod.CASH);
        }

        double distance = calculateDistance(
                request.getPickupLatitude(), request.getPickupLongitude(),
                request.getDropoffLatitude(), request.getDropoffLongitude()
        );

        double fare = request.getVehicleType().calculateFare(distance);

        Ride ride = Ride.builder()
                .userId(request.getUserId())
                .pickupLatitude(request.getPickupLatitude())
                .pickupLongitude(request.getPickupLongitude())
                .pickupAddress(request.getPickupAddress())
                .dropoffLatitude(request.getDropoffLatitude())
                .dropoffLongitude(request.getDropoffLongitude())
                .dropoffAddress(request.getDropoffAddress())
                .vehicleType(request.getVehicleType())
                .paymentMethod(request.getPaymentMethod())
                .estimatedFare(fare)
                .distance(distance)
                .status(RideStatus.REQUESTED)
                .scheduledRide(request.getScheduledRide())
                .scheduledTime(request.getScheduledTime())
                .promoCode(request.getPromoCode())
                .notes(request.getNotes())
                .build();

        ride = rideRepository.save(ride);

        log.info("Ride request created: Pickup({}) -> Dropoff({}), Fare: {}",
                request.getPickupAddress(), request.getDropoffAddress(), fare);

        return ride;
    }

    public Ride acceptRide(Long rideId, String driverId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if (ride.getStatus() != RideStatus.REQUESTED) {
            throw new RuntimeException("Ride is not available for acceptance");
        }

        ride.acceptRide(driverId);
        ride = rideRepository.save(ride);

        log.info("Ride {} accepted by driver {} at {}",
                rideId, driverId, ride.getRideAcceptedAt());

        return ride;
    }

    public Ride markDriverArrived(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.markDriverArrived();
        ride = rideRepository.save(ride);

        log.info("Driver arrived for ride {} at {}",
                rideId, ride.getDriverArrivedAt());

        return ride;
    }

    public Ride startRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.startRide();
        ride = rideRepository.save(ride);

        log.info("Ride {} started at {}", rideId, ride.getRideStartedAt());

        return ride;
    }

    public Ride completeRide(Long rideId, Double actualFare) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.completeRide(actualFare);
        ride = rideRepository.save(ride);

        log.info("Ride {} completed at {} with fare {}",
                rideId, ride.getRideCompletedAt(), actualFare);

        return ride;
    }

    public Ride cancelRide(Long rideId) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.cancelRide();
        ride = rideRepository.save(ride);

        log.info("Ride {} cancelled at {}", rideId, ride.getRideCancelledAt());

        return ride;
    }

    private void validateRideRequest(RideRequestDTO request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new RuntimeException("User ID is required");
        }

        if (request.getDropoffAddress() == null || request.getDropoffAddress().trim().isEmpty()) {
            throw new RuntimeException("Dropoff address is required");
        }

        if (request.getVehicleType() == null) {
            throw new RuntimeException("Vehicle type is required");
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}