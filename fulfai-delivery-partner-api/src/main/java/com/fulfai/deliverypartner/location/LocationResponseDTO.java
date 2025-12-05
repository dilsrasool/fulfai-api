package com.fulfai.deliverypartner.location;

import java.time.Instant;

import lombok.Data;

@Data
public class LocationResponseDTO {

    private String driverId;
    private Instant timestamp;
    private Double latitude;
    private Double longitude;
    private String geohash;
    private Double accuracy;
    private Double speed;
    private Double heading;
    private String status;
}
