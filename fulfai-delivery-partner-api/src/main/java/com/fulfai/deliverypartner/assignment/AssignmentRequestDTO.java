package com.fulfai.deliverypartner.assignment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignmentRequestDTO {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Selling Partner ID is required")
    private String sellingPartnerId;

    private String pickupAddress;
    private Double pickupLatitude;
    private Double pickupLongitude;
    private String deliveryAddress;
    private Double deliveryLatitude;
    private Double deliveryLongitude;
    private String notes;
}
