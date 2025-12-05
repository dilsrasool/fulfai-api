package com.fulfai.sellingpartner.order;

import java.math.BigDecimal;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@RegisterForReflection
public class OrderRequestDTO {

    private String branchId;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemDTO> items;

    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    @Size(max = 50, message = "Payment method must be less than 50 characters")
    private String paymentMethod;

    @Size(max = 20, message = "Payment status must be less than 20 characters")
    private String paymentStatus;

    @Size(max = 500, message = "Notes must be less than 500 characters")
    private String notes;
}
