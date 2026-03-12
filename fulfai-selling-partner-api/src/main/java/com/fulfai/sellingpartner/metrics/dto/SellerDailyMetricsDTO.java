package com.fulfai.sellingpartner.metrics.dto;

public record SellerDailyMetricsDTO(
        String date,
        int orders,
        double revenue
) {}
