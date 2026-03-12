package com.fulfai.sellingpartner.metrics.dto;

public record LowStockDTO(
        String branchId,
        String productId,
        int stock,
        int reorderLevel
) {}
