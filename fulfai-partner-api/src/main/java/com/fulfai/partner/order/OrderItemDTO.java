package com.fulfai.partner.order;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@RegisterForReflection
public class OrderItemDTO {

    @NotBlank(message = "Product ID cannot be blank")
    private String productId;

    @NotBlank(message = "Product name cannot be blank")
    private String productName;

    private String sku;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;

    private BigDecimal totalPrice;
}
