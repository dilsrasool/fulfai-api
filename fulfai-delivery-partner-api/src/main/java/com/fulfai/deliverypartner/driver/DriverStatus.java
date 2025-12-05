package com.fulfai.deliverypartner.driver;

public enum DriverStatus {
    AVAILABLE,   // Ready to accept orders
    BUSY,        // Currently on a delivery
    OFFLINE;     // Not working

    public static DriverStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        try {
            return DriverStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
