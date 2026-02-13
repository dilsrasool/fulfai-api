package com.fulfai.sellingpartner.analytics.dto;

public record OrderStatusSummaryDTO(
        int pending,
        int packed,
        int shipped,
        int delivered,
        int cancelled,
        int returned
) {}
