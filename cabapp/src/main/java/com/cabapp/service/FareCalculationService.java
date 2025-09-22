package com.cabapp.service;

import com.cabapp.model.VehicleType;
import com.cabapp.model.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Slf4j
public class FareCalculationService {

    // Base fare constants
    private static final double BASE_FARE = 50.0; // Minimum fare in rupees
    private static final double BOOKING_FEE = 10.0; // Platform booking fee

    // Time-based multipliers
    private static final double PEAK_HOUR_MULTIPLIER = 1.5; // 6-10 AM, 5-9 PM
    private static final double NIGHT_MULTIPLIER = 1.3; // 10 PM - 6 AM
    private static final double WEEKEND_MULTIPLIER = 1.2; // Saturday, Sunday

    // Distance thresholds
    private static final double FREE_CANCELLATION_DISTANCE = 2.0; // km
    private static final double LONG_DISTANCE_THRESHOLD = 50.0; // km
    private static final double LONG_DISTANCE_DISCOUNT = 0.9; // 10% discount for long rides

    // Calculate total fare for a ride
    public FareBreakdown calculateFare(VehicleType vehicleType, double distanceKm,
                                       LocalDateTime rideTime, String promoCode) {

        // Base calculation using vehicle type
        double baseFare = vehicleType.calculateFare(distanceKm);

        // Time-based multipliers
        double timeMultiplier = getTimeMultiplier(rideTime);
        double adjustedFare = baseFare * timeMultiplier;

        // Long distance discount
        if (distanceKm > LONG_DISTANCE_THRESHOLD) {
            adjustedFare *= LONG_DISTANCE_DISCOUNT;
        }

        // Add booking fee
        double totalBeforePromo = adjustedFare + BOOKING_FEE;

        // Apply promo code discount
        double promoDiscount = calculatePromoDiscount(totalBeforePromo, promoCode);
        double finalFare = totalBeforePromo - promoDiscount;

        // Ensure minimum fare
        finalFare = Math.max(finalFare, BASE_FARE);

        // Round to nearest rupee
        finalFare = Math.round(finalFare * 100.0) / 100.0;

        log.info("Calculated fare: Vehicle={}, Distance={}km, Time={}, Final=₹{}",
                vehicleType, distanceKm, rideTime, finalFare);

        return FareBreakdown.builder()
                .vehicleType(vehicleType)
                .distanceKm(distanceKm)
                .baseFare(baseFare)
                .timeMultiplier(timeMultiplier)
                .adjustedFare(adjustedFare)
                .bookingFee(BOOKING_FEE)
                .promoCode(promoCode)
                .promoDiscount(promoDiscount)
                .totalFare(finalFare)
                .currency("INR")
                .build();
    }

    // Calculate fare for immediate booking (current time)
    public FareBreakdown calculateFare(VehicleType vehicleType, double distanceKm, String promoCode) {
        return calculateFare(vehicleType, distanceKm, LocalDateTime.now(), promoCode);
    }

    // Calculate fare without promo code
    public FareBreakdown calculateFare(VehicleType vehicleType, double distanceKm) {
        return calculateFare(vehicleType, distanceKm, LocalDateTime.now(), null);
    }

    // Get time-based multiplier
    private double getTimeMultiplier(LocalDateTime rideTime) {
        LocalTime time = rideTime.toLocalTime();
        int dayOfWeek = rideTime.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday

        double multiplier = 1.0;

        // Peak hours: 6-10 AM, 5-9 PM
        if ((time.isAfter(LocalTime.of(6, 0)) && time.isBefore(LocalTime.of(10, 0))) ||
                (time.isAfter(LocalTime.of(17, 0)) && time.isBefore(LocalTime.of(21, 0)))) {
            multiplier = PEAK_HOUR_MULTIPLIER;
        }
        // Night hours: 10 PM - 6 AM
        else if (time.isAfter(LocalTime.of(22, 0)) || time.isBefore(LocalTime.of(6, 0))) {
            multiplier = NIGHT_MULTIPLIER;
        }

        // Weekend multiplier (Saturday=6, Sunday=7)
        if (dayOfWeek == 6 || dayOfWeek == 7) {
            multiplier *= WEEKEND_MULTIPLIER;
        }

        return multiplier;
    }

    // Calculate promo code discount
    private double calculatePromoDiscount(double totalFare, String promoCode) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return 0.0;
        }

        // Simple promo code logic - you can enhance this
        switch (promoCode.toUpperCase()) {
            case "FIRST10":
                return Math.min(totalFare * 0.10, 50.0); // 10% off, max ₹50
            case "ATLANTA20":
                return Math.min(totalFare * 0.20, 100.0); // 20% off, max ₹100
            case "NEWUSER":
                return Math.min(totalFare * 0.15, 75.0); // 15% off, max ₹75
            case "WEEKEND":
                return Math.min(totalFare * 0.12, 60.0); // 12% off, max ₹60
            case "SAVE50":
                return Math.min(50.0, totalFare * 0.5); // Flat ₹50 off or 50%, whichever is less
            default:
                log.warn("Unknown promo code: {}", promoCode);
                return 0.0;
        }
    }

    // Calculate cancellation fee
    public double calculateCancellationFee(VehicleType vehicleType, double driverDistanceKm,
                                           int minutesSinceBooking) {

        // Free cancellation within first 2 minutes
        if (minutesSinceBooking <= 2) {
            return 0.0;
        }

        // Free cancellation if driver is far away
        if (driverDistanceKm > FREE_CANCELLATION_DISTANCE) {
            return 0.0;
        }

        // Base cancellation fee based on vehicle type
        double cancellationFee = vehicleType.getBaseRatePerKm() * 2; // 2x the per-km rate

        // Minimum and maximum limits
        cancellationFee = Math.max(cancellationFee, 20.0); // Min ₹20
        cancellationFee = Math.min(cancellationFee, 100.0); // Max ₹100

        return Math.round(cancellationFee * 100.0) / 100.0;
    }

    // Estimate fare for given pickup and dropoff coordinates
    public FareBreakdown estimateFare(double pickupLat, double pickupLng,
                                      double dropoffLat, double dropoffLng,
                                      VehicleType vehicleType, String promoCode) {

        // Calculate distance
        double distance = calculateDistance(pickupLat, pickupLng, dropoffLat, dropoffLng);

        return calculateFare(vehicleType, distance, promoCode);
    }

    // Haversine formula for distance calculation
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // Fare breakdown class
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FareBreakdown {
        private VehicleType vehicleType;
        private double distanceKm;
        private double baseFare;
        private double timeMultiplier;
        private double adjustedFare;
        private double bookingFee;
        private String promoCode;
        private double promoDiscount;
        private double totalFare;
        private String currency;

        public String getFormattedBreakdown() {
            StringBuilder sb = new StringBuilder();
            sb.append("Fare Breakdown:\n");
            sb.append(String.format("Base Fare (%.1fkm): ₹%.2f\n", distanceKm, baseFare));
            if (timeMultiplier != 1.0) {
                sb.append(String.format("Time Multiplier (%.1fx): ₹%.2f\n", timeMultiplier, adjustedFare));
            }
            sb.append(String.format("Booking Fee: ₹%.2f\n", bookingFee));
            if (promoDiscount > 0) {
                sb.append(String.format("Promo Discount (%s): -₹%.2f\n", promoCode, promoDiscount));
            }
            sb.append(String.format("Total: ₹%.2f", totalFare));
            return sb.toString();
        }
    }
}