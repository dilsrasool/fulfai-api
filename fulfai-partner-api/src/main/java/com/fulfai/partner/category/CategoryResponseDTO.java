package com.fulfai.partner.category;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class CategoryResponseDTO {

    private String companyId;
    private String name;
    private String description;
    private String imageUrl;
    private Integer displayOrder;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
