package com.fulfai.deliverypartner.assignment;

import java.util.List;

public enum AssignmentStatus {
    ASSIGNED,       // Order assigned to driver
    PICKED_UP,      // Driver picked up the order
    IN_TRANSIT,     // Driver is on the way to delivery
    DELIVERED,      // Order delivered
    CANCELLED;      // Assignment cancelled

    public static AssignmentStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        try {
            return AssignmentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Get allowed transitions from current status.
     */
    public static List<String> getAllowedFromStatuses(String targetStatus) {
        AssignmentStatus target = fromString(targetStatus);
        if (target == null) {
            return List.of();
        }

        return switch (target) {
            case ASSIGNED -> List.of();  // Initial state
            case PICKED_UP -> List.of("ASSIGNED");
            case IN_TRANSIT -> List.of("PICKED_UP");
            case DELIVERED -> List.of("IN_TRANSIT");
            case CANCELLED -> List.of("ASSIGNED", "PICKED_UP");  // Can cancel before delivery
        };
    }
}
