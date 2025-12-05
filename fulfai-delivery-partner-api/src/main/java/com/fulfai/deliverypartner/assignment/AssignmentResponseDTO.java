package com.fulfai.deliverypartner.assignment;

import java.time.Instant;

import lombok.Data;

@Data
public class AssignmentResponseDTO {

    private String driverId;
    private Instant assignedAt;
    private String orderId;
    private String sellingPartnerId;
    private String status;
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
}
