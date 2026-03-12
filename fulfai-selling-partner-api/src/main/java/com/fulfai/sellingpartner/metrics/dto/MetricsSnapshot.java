package com.fulfai.sellingpartner.metrics.dto;

public record MetricsSnapshot(
        int orders,
        int units,
        double revenue,
        double profit
) {}
