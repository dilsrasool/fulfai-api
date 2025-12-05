package com.fulfai.deliverypartner.location;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationUpdateDTO {

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double accuracy;    // GPS accuracy in meters
    private Double speed;       // Speed in km/h
    private Double heading;     // Direction in degrees (0-360)
}
