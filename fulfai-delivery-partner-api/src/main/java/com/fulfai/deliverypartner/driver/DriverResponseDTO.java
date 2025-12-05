package com.fulfai.deliverypartner.driver;

import java.time.Instant;

import lombok.Data;

@Data
public class DriverResponseDTO {

    private String companyId;
    private String driverId;
    private String name;
    private String phoneNumber;
    private String email;
    private String vehicleType;
    private String vehicleNumber;
    private String licenseNumber;
    private String status;
    private String currentCity;
    private Double lastLatitude;
    private Double lastLongitude;
    private Instant lastLocationUpdate;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
