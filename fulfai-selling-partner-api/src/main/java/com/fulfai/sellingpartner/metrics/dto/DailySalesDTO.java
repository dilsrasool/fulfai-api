package com.fulfai.sellingpartner.metrics.dto;

public record DailySalesDTO(
        String date,
        int orders,
        double revenue,
        double profit
) {}
