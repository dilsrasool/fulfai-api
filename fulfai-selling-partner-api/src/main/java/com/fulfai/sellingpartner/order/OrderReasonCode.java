package com.fulfai.sellingpartner.order;

import java.util.Set;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum OrderReasonCode {
    VENDOR_REJECTED_OUT_OF_STOCK,
    VENDOR_REJECTED_CLOSED,
    CUSTOMER_CANCELLED,
    VENDOR_CANCELLED,
    ADMIN_CANCELLED,
    CUSTOMER_CHANGE_REQUEST,
    VENDOR_CHANGE_APPROVED,
    VENDOR_CHANGE_REJECTED,
    REFUND_CUSTOMER_REQUEST,
    REFUND_ITEM_UNAVAILABLE,
    REFUND_DELIVERY_FAILURE,
    ISSUE_DAMAGED,
    ISSUE_MISSING_ITEM,
    ISSUE_WRONG_ITEM,
    ISSUE_LATE_DELIVERY,
    DELIVERY_ADDRESS_UNREACHABLE,
    DELIVERY_CUSTOMER_UNAVAILABLE,
    DELIVERY_DRIVER_INCIDENT,
    DELIVERY_REATTEMPT_SCHEDULED;

    public static OrderReasonCode fromNullable(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return valueOf(raw.trim().toUpperCase());
    }

    public static final Set<OrderReasonCode> REJECT_REASON_CODES = Set.of(
            VENDOR_REJECTED_OUT_OF_STOCK,
            VENDOR_REJECTED_CLOSED
    );

    public static final Set<OrderReasonCode> CANCEL_REASON_CODES = Set.of(
            CUSTOMER_CANCELLED,
            VENDOR_CANCELLED,
            ADMIN_CANCELLED
    );

    public static final Set<OrderReasonCode> CHANGE_REASON_CODES = Set.of(
            CUSTOMER_CHANGE_REQUEST,
            VENDOR_CHANGE_APPROVED,
            VENDOR_CHANGE_REJECTED
    );

    public static final Set<OrderReasonCode> REFUND_REASON_CODES = Set.of(
            REFUND_CUSTOMER_REQUEST,
            REFUND_ITEM_UNAVAILABLE,
            REFUND_DELIVERY_FAILURE
    );
}