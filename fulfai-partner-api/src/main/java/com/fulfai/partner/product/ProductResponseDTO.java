package com.fulfai.partner.product;

import java.math.BigDecimal;
import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class ProductResponseDTO {

    private String companyId;
    private String productId;
    private String name;
    private String description;
    private String category;
    private String sku;
    private String barcode;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String unit;
    private Integer stockQuantity;
    private Integer reorderLevel;
    private String imageUrl;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
