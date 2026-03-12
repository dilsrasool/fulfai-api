package com.fulfai.sellingpartner.metrics.dto;

public record SellerKpiDTO(
        int orders,
        double revenue,
        double profit,
        int units
) {}
