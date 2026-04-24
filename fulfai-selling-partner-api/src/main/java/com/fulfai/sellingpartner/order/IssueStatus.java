package com.fulfai.sellingpartner.order;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum IssueStatus {
    ISSUE_REPORTED("issue_reported"),
    INVESTIGATING("investigating"),
    RESOLVED_REFUND("resolved_refund"),
    RESOLVED_REDELIVERY("resolved_redelivery"),
    RESOLVED_REPLACEMENT("resolved_replacement"),
    REJECTED_CLAIM("rejected_claim");

    private final String value;

    IssueStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static IssueStatus fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        for (IssueStatus status : values()) {
            if (status.value.equalsIgnoreCase(raw) || status.name().equalsIgnoreCase(raw)) {
                return status;
            }
        }

        return null;
    }
}