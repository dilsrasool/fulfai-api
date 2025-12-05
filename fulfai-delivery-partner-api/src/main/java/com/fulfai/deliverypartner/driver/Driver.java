package com.fulfai.deliverypartner.driver;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Driver/Rider entity representing delivery personnel.
 * PK: companyId, SK: driverId
 * GSI: status-index (status as PK) for finding available drivers
 */
@Data
@DynamoDbBean
@RegisterForReflection
public class Driver {

    public static final String STATUS_GSI = "status-index";

    private String companyId;
    private String driverId;
    private String name;
    private String phoneNumber;
    private String email;
    private String vehicleType;      // BIKE, CAR, VAN, TRUCK
    private String vehicleNumber;
    private String licenseNumber;
    private String status;           // AVAILABLE, BUSY, OFFLINE
    private String currentCity;
    private Double lastLatitude;
    private Double lastLongitude;
    private Instant lastLocationUpdate;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("driverId")
    public String getDriverId() {
        return driverId;
    }

    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }

    @DynamoDbAttribute("phoneNumber")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @DynamoDbAttribute("email")
    public String getEmail() {
        return email;
    }

    @DynamoDbAttribute("vehicleType")
    public String getVehicleType() {
        return vehicleType;
    }

    @DynamoDbAttribute("vehicleNumber")
    public String getVehicleNumber() {
        return vehicleNumber;
    }

    @DynamoDbAttribute("licenseNumber")
    public String getLicenseNumber() {
        return licenseNumber;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = STATUS_GSI)
    @DynamoDbAttribute("status")
    public String getStatus() {
        return status;
    }

    @DynamoDbAttribute("currentCity")
    public String getCurrentCity() {
        return currentCity;
    }

    @DynamoDbAttribute("lastLatitude")
    public Double getLastLatitude() {
        return lastLatitude;
    }

    @DynamoDbAttribute("lastLongitude")
    public Double getLastLongitude() {
        return lastLongitude;
    }

    @DynamoDbAttribute("lastLocationUpdate")
    public Instant getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    @DynamoDbAttribute("isActive")
    public Boolean getIsActive() {
        return isActive;
    }

    @DynamoDbAttribute("createdAt")
    public Instant getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("updatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
