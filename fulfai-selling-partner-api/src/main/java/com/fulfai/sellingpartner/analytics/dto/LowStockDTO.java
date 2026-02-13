package com.fulfai.sellingpartner.analytics.dto;

public record LowStockDTO(
        String branchId,
        String productId,
        int stock,
        int reorderLevel
) {}
