package com.fulfai.sellingpartner.order;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@RegisterForReflection
public class OrderSearchDTO {
    @NotNull(message = "Start date is required")
    private Instant startDate;  // UTC timestamp
    @NotNull(message = "End date is required")
    private Instant endDate;    // UTC timestamp
    private String nextToken;   // Pagination token
    private Integer limit;      // Page size (default 20)
}
