package com.fulfai.sellingpartner.metrics.dto;

public record OrderStatusSnapshot(
        int pending,
        int packed,
        int shipped,
        int delivered,
        int cancelled,
        int returned
) {}
