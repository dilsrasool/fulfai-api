package com.fulfai.deliverypartner.driver;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DriverRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @Email(message = "Invalid email format")
    private String email;

    private String vehicleType;      // BIKE, CAR, VAN, TRUCK
    private String vehicleNumber;
    private String licenseNumber;
    private String currentCity;
    private Boolean isActive;
}
