package com.fulfai.partner.product;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class ProductSearchDTO {
    private String category;    // Category to filter by
    private String nextToken;   // Pagination token
    private Integer limit;      // Page size (default 20)
}
