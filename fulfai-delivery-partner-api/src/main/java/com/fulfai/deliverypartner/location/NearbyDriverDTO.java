package com.fulfai.deliverypartner.location;

import java.time.Instant;

import lombok.Data;

@Data
public class NearbyDriverDTO {

    private String driverId;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private String status;
    private Instant lastUpdate;
}
