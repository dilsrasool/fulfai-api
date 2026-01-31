package com.fulfai.sellingpartner.product;

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

    /* =========================
       IMAGE STATUS
    ========================= */

    public enum ImageProcessingStatus {
        NOT_UPLOADED,
        UPLOADING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    /* =========================
       CONSTANTS
    ========================= */

    public static final String CATEGORY_GSI = "category-index";

    /* =========================
       KEYS
    ========================= */

    private String companyId;
    private String branchProductKey; // branchId#productId
    private String branchId;
    private String productId;

    /* =========================
       COMPANY INFO (DENORMALIZED)
    ========================= */

    private String companyName;
    private String companyLogo;

    /* =========================
       PRODUCT INFO
    ========================= */

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

    /* =========================
       IMAGE INFO
    ========================= */

    private String imageUrl;
    private String thumbnailUrl;
    private ImageProcessingStatus imageProcessingStatus;
    private String imageUploadId;
    private String imageError;

    /* =========================
       STATUS / LOCATION
    ========================= */

    private Boolean isActive;
    private Double longitude;
    private Double latitude;

    /* =========================
       TIMESTAMPS
    ========================= */

    private Instant createdAt;
    private Instant updatedAt;

    /* =========================
       DYNAMODB MAPPINGS
    ========================= */

    @DynamoDbPartitionKey
    @DynamoDbSecondarySortKey(indexNames = CATEGORY_GSI)
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("branchProductKey")
    public String getBranchProductKey() {
        return branchProductKey;
    }

    @DynamoDbAttribute("branchId")
    public String getBranchId() {
        return branchId;
    }

    @DynamoDbAttribute("productId")
    public String getProductId() {
        return productId;
    }

    @DynamoDbAttribute("companyName")
    public String getCompanyName() {
        return companyName;
    }

    @DynamoDbAttribute("companyLogo")
    public String getCompanyLogo() {
        return companyLogo;
    }

    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }

    @DynamoDbAttribute("description")
    public String getDescription() {
        return description;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = CATEGORY_GSI)
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

    @DynamoDbAttribute("thumbnailUrl")
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @DynamoDbAttribute("imageProcessingStatus")
    public ImageProcessingStatus getImageProcessingStatus() {
        return imageProcessingStatus;
    }

    @DynamoDbAttribute("imageUploadId")
    public String getImageUploadId() {
        return imageUploadId;
    }

    @DynamoDbAttribute("imageError")
    public String getImageError() {
        return imageError;
    }

    @DynamoDbAttribute("isActive")
    public Boolean getIsActive() {
        return isActive;
    }

    @DynamoDbAttribute("longitude")
    public Double getLongitude() {
        return longitude;
    }

    @DynamoDbAttribute("latitude")
    public Double getLatitude() {
        return latitude;
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
