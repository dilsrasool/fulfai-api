package com.fulfai.sellingpartner.order;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public enum OrderStatus {
    RECEIVED(Arrays.asList("ACCEPTED", "CANCELLED")),
    ACCEPTED(Arrays.asList("PREPARED", "CANCELLED")),
    PREPARED(Arrays.asList("RIDER_ACCEPTED", "CANCELLED")),
    RIDER_ACCEPTED(Arrays.asList("SHIPPED", "CANCELLED")),
    SHIPPED(Arrays.asList("DELIVERED", "CANCELLED")),
    DELIVERED(Collections.emptyList()),
    CANCELLED(Collections.emptyList());

    private final List<String> allowedTransitions;

    OrderStatus(List<String> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public List<String> getAllowedTransitions() {
        return allowedTransitions;
    }

    public boolean canTransitionTo(String targetStatus) {
        return allowedTransitions.contains(targetStatus);
    }

    public boolean canTransitionTo(OrderStatus targetStatus) {
        return allowedTransitions.contains(targetStatus.name());
    }

    public static OrderStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean isValidStatus(String status) {
        return fromString(status) != null;
    }

    public static List<String> getAllowedTransitionsFrom(String currentStatus) {
        OrderStatus status = fromString(currentStatus);
        if (status == null) {
            return Collections.emptyList();
        }
        return status.getAllowedTransitions();
    }

    /**
     * Get all statuses that can transition TO the given target status.
     * This is the reverse lookup - given a target, what are valid "from" statuses.
     */
    public static List<String> getAllowedFromStatuses(String targetStatus) {
        List<String> allowedFrom = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            if (status.getAllowedTransitions().contains(targetStatus)) {
                allowedFrom.add(status.name());
            }
        }
        return allowedFrom;
    }

    public static void validateTransition(String currentStatus, String targetStatus) {
        OrderStatus current = fromString(currentStatus);
        OrderStatus target = fromString(targetStatus);

        if (current == null) {
            throw new InvalidOrderStatusTransitionException(
                    "Invalid current status: " + currentStatus);
        }

        if (target == null) {
            throw new InvalidOrderStatusTransitionException(
                    "Invalid target status: " + targetStatus);
        }

        if (!current.canTransitionTo(target)) {
            throw new InvalidOrderStatusTransitionException(
                    String.format("Cannot transition from %s to %s. Allowed transitions: %s",
                            currentStatus, targetStatus, current.getAllowedTransitions()));
        }
    }

    public static class InvalidOrderStatusTransitionException extends RuntimeException {
        public InvalidOrderStatusTransitionException(String message) {
            super(message);
        }
    }
}
