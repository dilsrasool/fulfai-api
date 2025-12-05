package com.fulfai.partner.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class OrderResponseDTO {

    private String companyId;
    private String orderId;
    private String orderDate;
    private String status;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private String branchId;
    private List<OrderItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;
}
