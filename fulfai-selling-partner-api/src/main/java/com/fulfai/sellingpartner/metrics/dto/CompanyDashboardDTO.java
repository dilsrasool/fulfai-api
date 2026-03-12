package com.fulfai.sellingpartner.metrics.dto;

import java.util.List;

public record CompanyDashboardDTO(
        SellerKpiDTO kpis,
        List<DailySalesDTO> salesTrend,
        List<TopProductDTO> topProducts
) {}
