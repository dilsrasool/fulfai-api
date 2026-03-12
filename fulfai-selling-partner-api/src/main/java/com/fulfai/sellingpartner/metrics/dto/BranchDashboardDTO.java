package com.fulfai.sellingpartner.metrics.dto;

import java.util.List;

public record BranchDashboardDTO(
        List<InventorySnapshotDTO> inventory,
        List<LowStockDTO> lowStock,
        List<OrderStatusSummaryDTO> orderTrend
) {}
