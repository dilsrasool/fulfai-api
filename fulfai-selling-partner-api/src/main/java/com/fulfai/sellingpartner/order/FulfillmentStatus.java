package com.fulfai.sellingpartner.order;

import java.util.Locale;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum FulfillmentStatus {
    CREATED("created"),
    ACCEPTED("accepted"),
    PREPARING("preparing"),
    READY("ready"),
    PICKED_UP("picked_up"),
    ON_THE_WAY("on_the_way"),
    DELIVERED("delivered"),
    CANCELLED("cancelled"),
    FAILED("failed"),
    RETURNED("returned"),
    REFUNDED("refunded");

    private final String value;

    FulfillmentStatus(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static FulfillmentStatus fromValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        FulfillmentStatus legacyMapped = switch (normalized) {
            case "RECEIVED", "PENDING", "PENDING_VENDOR_ACCEPTANCE" -> CREATED;
            case "PREPARED", "READY_FOR_PICKUP" -> READY;
            case "COURIER_ASSIGNED", "SHIPPED", "REATTEMPTING_DELIVERY" -> ON_THE_WAY;
            case "DELIVERY_FAILED" -> FAILED;
            case "COMPLETED" -> DELIVERED;
            case "CANCEL_REQUESTED", "REJECTED" -> CANCELLED;
            case "CHANGE_REQUESTED" -> ACCEPTED;
            case "CHANGED" -> PREPARING;
            default -> null;
        };
        if (legacyMapped != null) {
            return legacyMapped;
        }

        for (FulfillmentStatus status : values()) {
            if (status.value.equalsIgnoreCase(raw) || status.name().equalsIgnoreCase(raw)) {
                return status;
            }
        }

        return null;
    }
}