package com.cabapp.model;

import lombok.Getter;

@Getter
public enum VehicleType {
    HATCHBACK("Hatchback", 4, 10.0, "Maruti Swift, Hyundai i20"),
    SEDAN("Sedan", 4, 12.0, "Honda City, Maruti Dzire"),
    SUV("SUV", 6, 15.0, "Mahindra XUV500, Tata Safari"),
    MINI("Mini", 4, 8.0, "Tata Nano, Maruti Alto"),
    PREMIUM("Premium", 4, 20.0, "BMW, Audi, Mercedes"),
    LUXURY("Luxury", 4, 30.0, "Rolls Royce, Bentley"),
    AUTO_RICKSHAW("Auto", 3, 5.0, "3-Wheeler Auto Rickshaw"),
    BIKE("Bike", 2, 3.0, "Motorcycle/Scooter");

    private final String displayName;
    private final int maxCapacity;
    private final double baseRatePerKm; // Base rate per kilometer
    private final String examples;

    VehicleType(String displayName, int maxCapacity, double baseRatePerKm, String examples) {
        this.displayName = displayName;
        this.maxCapacity = maxCapacity;
        this.baseRatePerKm = baseRatePerKm;
        this.examples = examples;
    }

    // Calculate fare based on distance and vehicle type
    public double calculateFare(double distanceInKm) {
        double baseFare = 50.0; // Minimum fare
        double distanceFare = distanceInKm * this.baseRatePerKm;
        return baseFare + distanceFare;
    }

    // Check if vehicle can accommodate passengers
    public boolean canAccommodate(int passengerCount) {
        return passengerCount <= this.maxCapacity;
    }
}