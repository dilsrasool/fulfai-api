package com.fulfai.sellingpartner.order;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum PaymentStatus {
    PAYMENT_AUTHORIZED("payment_authorized"),
    PAYMENT_CAPTURED("payment_captured"),
    REFUND_PENDING("refund_pending"),
    PARTIALLY_REFUNDED("partially_refunded"),
    REFUNDED("refunded"),
    CHARGEBACK_OPEN("chargeback_open"),
    CHARGEBACK_WON("chargeback_won"),
    CHARGEBACK_LOST("chargeback_lost");

    private final String value;

    PaymentStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static PaymentStatus fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        for (PaymentStatus status : values()) {
            if (status.value.equalsIgnoreCase(raw) || status.name().equalsIgnoreCase(raw)) {
                return status;
            }
        }

        // Legacy values from existing data.
        if ("PENDING".equalsIgnoreCase(raw) || "UNPAID".equalsIgnoreCase(raw)) {
            return PAYMENT_AUTHORIZED;
        }
        if ("PAID".equalsIgnoreCase(raw)) {
            return PAYMENT_CAPTURED;
        }

        return null;
    }
}