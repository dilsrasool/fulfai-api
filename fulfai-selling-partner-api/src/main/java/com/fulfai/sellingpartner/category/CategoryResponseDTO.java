package com.fulfai.sellingpartner.category;

import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class CategoryResponseDTO {

    // ---------- Identifiers ----------
    private String categoryId;
    private String parentCategoryId;

    // ---------- Business Fields ----------
    private String name;
    private List<String> parentCategories;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isActive;

    // ---------- Metadata ----------
    private Instant createdAt;
    private Instant updatedAt;
}
