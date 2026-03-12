package com.fulfai.sellingpartner.metrics.dto;

public record ProductDailySalesDTO(
        String date,
        int units,
        double revenue
) {}
