package com.fulfai.sellingpartner.analytics.dto;

import java.util.List;

public record BranchDashboardDTO(

        double revenue,
        int orders,
        int averageOrderValue,

        List<DailyPointDTO> trend,
        List<TopProductDTO> topProducts,
        List<LowStockDTO> lowStock,

        OrderStatusSummaryDTO statusSummary

) {}
