package com.fulfai.sellingpartner.analytics.dto;

public record TopProductDTO(
        String productId,
        int unitsSold,
        double revenue
) {}
