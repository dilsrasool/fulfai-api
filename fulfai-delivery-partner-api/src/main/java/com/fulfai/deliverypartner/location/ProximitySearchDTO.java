package com.fulfai.deliverypartner.location;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProximitySearchDTO {

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private Double radiusKm;        // Search radius in km (default 5km)
    private Integer maxResults;     // Max drivers to return (default 10)
    private String status;          // Filter by driver status (optional)
}
