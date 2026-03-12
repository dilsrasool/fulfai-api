package com.fulfai.sellingpartner.metrics.dto;

public record ProductMetricsSnapshot(
        String productId,
        int units,
        double revenue,
        int stock
) {}
