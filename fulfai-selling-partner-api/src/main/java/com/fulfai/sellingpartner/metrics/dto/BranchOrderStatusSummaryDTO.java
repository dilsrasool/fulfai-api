package com.fulfai.sellingpartner.metrics.dto;

public record BranchOrderStatusSummaryDTO(
        String branchId,
        int pending,
        int confirmed,
        int preparing,
        int shipped,
        int delivered,
        int cancelled
) {}

