package com.fulfai.partner.product;

import java.math.BigDecimal;
import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@DynamoDbBean
@RegisterForReflection
public class Product {

    public static final String CATEGORY_GSI = "category-index";

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

    @DynamoDbPartitionKey
    @DynamoDbSecondaryPartitionKey(indexNames = CATEGORY_GSI)
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("productId")
    public String getProductId() {
        return productId;
    }

    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }

    @DynamoDbAttribute("description")
    public String getDescription() {
        return description;
    }

    @DynamoDbSecondarySortKey(indexNames = CATEGORY_GSI)
    @DynamoDbAttribute("category")
    public String getCategory() {
        return category;
    }

    @DynamoDbAttribute("sku")
    public String getSku() {
        return sku;
    }

    @DynamoDbAttribute("barcode")
    public String getBarcode() {
        return barcode;
    }

    @DynamoDbAttribute("price")
    public BigDecimal getPrice() {
        return price;
    }

    @DynamoDbAttribute("costPrice")
    public BigDecimal getCostPrice() {
        return costPrice;
    }

    @DynamoDbAttribute("unit")
    public String getUnit() {
        return unit;
    }

    @DynamoDbAttribute("stockQuantity")
    public Integer getStockQuantity() {
        return stockQuantity;
    }

    @DynamoDbAttribute("reorderLevel")
    public Integer getReorderLevel() {
        return reorderLevel;
    }

    @DynamoDbAttribute("imageUrl")
    public String getImageUrl() {
        return imageUrl;
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
