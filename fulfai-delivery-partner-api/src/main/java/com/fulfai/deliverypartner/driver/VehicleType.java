package com.fulfai.deliverypartner.driver;

public enum VehicleType {
    BIKE,
    CAR,
    VAN,
    TRUCK;

    public static VehicleType fromString(String type) {
        if (type == null) {
            return null;
        }
        try {
            return VehicleType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
