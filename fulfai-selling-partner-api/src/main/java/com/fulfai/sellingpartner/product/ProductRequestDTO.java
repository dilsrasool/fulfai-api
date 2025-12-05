package com.fulfai.sellingpartner.product;

import java.math.BigDecimal;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@RegisterForReflection
public class ProductRequestDTO {

    @NotBlank(message = "Company name cannot be blank")
    @Size(max = 200, message = "Company name must be less than 200 characters")
    private String companyName;

    @Size(max = 500, message = "Company logo URL must be less than 500 characters")
    private String companyLogo;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 200, message = "Name must be less than 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotBlank(message = "Category cannot be blank")
    @Size(max = 100, message = "Category must be less than 100 characters")
    private String category;

    @Size(max = 50, message = "SKU must be less than 50 characters")
    private String sku;

    @Size(max = 50, message = "Barcode must be less than 50 characters")
    private String barcode;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @PositiveOrZero(message = "Cost price must be zero or positive")
    private BigDecimal costPrice;

    @Size(max = 20, message = "Unit must be less than 20 characters")
    private String unit;

    @PositiveOrZero(message = "Stock quantity must be zero or positive")
    private Integer stockQuantity;

    @PositiveOrZero(message = "Reorder level must be zero or positive")
    private Integer reorderLevel;

    @Size(max = 500, message = "Image URL must be less than 500 characters")
    private String imageUrl;

    private Boolean isActive;

    private Double longitude;

    private Double latitude;
}
