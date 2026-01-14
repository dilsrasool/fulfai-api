package com.fulfai.sellingpartner.category;

import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@DynamoDbBean
@RegisterForReflection
public class Category {

    public static final String PARENT_GSI = "parent-index";

    // ---------- Keys ----------
    private String companyId;
    private String categoryId;

    // ---------- Hierarchy ----------
    private String parentCategoryId;        // ROOT for top-level
    private List<String> parentCategories;  // ancestry path

    // ---------- Business ----------
    private String name;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isActive;

    private Instant createdAt;
    private Instant updatedAt;

    // ---------- PK ----------
    @DynamoDbPartitionKey
    public String getCompanyId() {
        return companyId;
    }

    // ---------- SK ----------
    @DynamoDbSortKey
    public String getCategoryId() {
        return categoryId;
    }

    // ---------- Parent GSI ----------
    @DynamoDbSecondaryPartitionKey(indexNames = PARENT_GSI)
    @DynamoDbSecondarySortKey(indexNames = PARENT_GSI)
    public String getParentCategoryId() {
        return parentCategoryId;
    }
}
