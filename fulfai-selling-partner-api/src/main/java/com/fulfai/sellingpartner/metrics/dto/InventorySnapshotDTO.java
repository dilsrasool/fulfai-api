package com.fulfai.sellingpartner.metrics.dto;

public record InventorySnapshotDTO(
        String branchId,
        String productId,
        int stock,
        int reserved,
        int reorderLevel,
        int daysOfInventory
) {}
