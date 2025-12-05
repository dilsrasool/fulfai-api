package com.fulfai.common.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Data
@RegisterForReflection
public class PaginationDTO {
    private String nextToken;   // Pagination token
    private Integer limit;      // Page size (default 20)
}
