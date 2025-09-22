package com.cabapp.model;

import lombok.Getter;

@Getter
public enum RideStatus {
    REQUESTED("Ride Requested", "Looking for driver"),
    ASSIGNED("Driver Assigned", "Driver is coming to pickup"),
    DRIVER_ARRIVED("Driver Arrived", "Driver has reached pickup location"),
    IN_PROGRESS("Ride Started", "Ride is in progress"),
    COMPLETED("Ride Completed", "Ride finished successfully"),
    CANCELLED("Ride Cancelled", "Ride was cancelled");

    private final String displayName;
    private final String description;

    RideStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}