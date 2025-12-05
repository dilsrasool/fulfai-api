package com.fulfai.sellingpartner.category;

import java.time.Instant;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class CategoryResponseDTO {

    private String name;
    private String parentCategory;
    private List<String> parentCategories;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
