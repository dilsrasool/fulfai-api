package com.fulfai.sellingpartner.metrics.dto;

public record OrderStatusSummaryDTO(
        int pending,
        int confirmed,
        int preparing,
        int shipped,
        int delivered,
        int cancelled
) {}


