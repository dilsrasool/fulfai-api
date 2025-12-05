package com.fulfai.sellingpartner.category;

import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@Data
@DynamoDbBean
@RegisterForReflection
public class Category {

    public static final String PARENT_GSI = "parent-index";

    private String name;
    private String parentCategory; // Immediate parent category name
    private List<String> parentCategories; // Full hierarchy of parent categories
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("name")
    public String getName() {
        return name;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = PARENT_GSI)
    @DynamoDbAttribute("parentCategory")
    public String getParentCategory() {
        return parentCategory;
    }

    @DynamoDbAttribute("parentCategories")
    public List<String> getParentCategories() {
        return parentCategories;
    }

    @DynamoDbAttribute("description")
    public String getDescription() {
        return description;
    }

    @DynamoDbAttribute("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    @DynamoDbAttribute("displayOrder")
    public Integer getDisplayOrder() {
        return displayOrder;
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
