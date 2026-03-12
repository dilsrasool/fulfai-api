package com.fulfai.sellingpartner.metrics.dto;

public record TopProductDTO(
        String productId,
        int units,
        double revenue
) {}
