package com.fulfai.deliverypartner.assignment;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Tracks which driver is assigned to which order.
 * PK: driverId, SK: assignedAt (to query driver's assignments by time)
 * GSI: order-index (orderId as PK) for looking up driver by order
 * GSI: status-index (status as PK, assignedAt as SK) for filtering by status
 */
@Data
@DynamoDbBean
@RegisterForReflection
public class DriverOrderAssignment {

    public static final String ORDER_GSI = "order-index";
    public static final String STATUS_GSI = "assignment-status-index";

    private String driverId;
    private Instant assignedAt;
    private String orderId;
    private String sellingPartnerId; // Company that created the order
    private String status; // ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, CANCELLED
    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String deliveryAddress;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("driverId")
    public String getDriverId() {
        return driverId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("assignedAt")
    public Instant getAssignedAt() {
        return assignedAt;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = ORDER_GSI)
    @DynamoDbAttribute("orderId")
    public String getOrderId() {
        return orderId;
    }

    @DynamoDbAttribute("sellingPartnerId")
    public String getSellingPartnerId() {
        return sellingPartnerId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = STATUS_GSI)
    @DynamoDbAttribute("status")
    public String getStatus() {
        return status;
    }

    @DynamoDbAttribute("pickupAddress")
    public String getPickupAddress() {
        return pickupAddress;
    }

    @DynamoDbAttribute("pickupLatitude")
    public Double getPickupLatitude() {
        return pickupLatitude;
    }

    @DynamoDbAttribute("pickupLongitude")
    public Double getPickupLongitude() {
        return pickupLongitude;
    }

    @DynamoDbAttribute("deliveryAddress")
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    @DynamoDbAttribute("deliveryLatitude")
    public Double getDeliveryLatitude() {
        return deliveryLatitude;
    }

    @DynamoDbAttribute("deliveryLongitude")
    public Double getDeliveryLongitude() {
        return deliveryLongitude;
    }

    @DynamoDbAttribute("pickedUpAt")
    public Instant getPickedUpAt() {
        return pickedUpAt;
    }

    @DynamoDbAttribute("deliveredAt")
    public Instant getDeliveredAt() {
        return deliveredAt;
    }

    @DynamoDbAttribute("notes")
    public String getNotes() {
        return notes;
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
