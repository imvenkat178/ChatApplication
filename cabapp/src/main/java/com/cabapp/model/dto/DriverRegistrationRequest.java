package com.cabapp.model.dto;

import com.cabapp.model.VehicleType;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverRegistrationRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$")
    private String password;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotBlank(message = "Vehicle make is required")
    private String vehicleMake;

    @NotBlank(message = "Vehicle model is required")
    private String vehicleModel;

    @NotNull(message = "Vehicle year is required")
    @Min(2000)
    @Max(2024)
    private Integer vehicleYear;

    @NotBlank(message = "Vehicle color is required")
    private String vehicleColor;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @Min(1)
    @Max(15)
    private Integer vehicleCapacity;

    private String deviceToken;

    private String bankAccountNumber;

    private String bankName;

    private String bankRoutingNumber;
}