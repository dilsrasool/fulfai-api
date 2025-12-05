package com.fulfai.partner.category;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@RegisterForReflection
public class CategoryRequestDTO {

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;

    @Size(max = 500, message = "Image URL must be less than 500 characters")
    private String imageUrl;

    private Integer displayOrder;

    private Boolean isActive;
}
