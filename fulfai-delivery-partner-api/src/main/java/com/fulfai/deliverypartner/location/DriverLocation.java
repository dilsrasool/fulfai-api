package com.fulfai.deliverypartner.location;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

/**
 * Tracks driver location history at configured frequency.
 * PK: driverId, SK: timestamp
 * GSI: geohash-index (geohash as PK, timestamp as SK) for proximity search
 *
 * Geohash is used for efficient geo-proximity queries.
 * A 6-character geohash provides ~1.2km precision.
 */
@Data
@DynamoDbBean
@RegisterForReflection
public class DriverLocation {

    public static final String GEOHASH_GSI = "geohash-index";

    private String driverId;
    private Instant timestamp;
    private Double latitude;
    private Double longitude;
    private String geohash;         // For proximity search (6 chars = ~1.2km precision)
    private Double accuracy;        // GPS accuracy in meters
    private Double speed;           // Speed in km/h
    private Double heading;         // Direction in degrees (0-360)
    private String status;          // Driver status at this point

    @DynamoDbPartitionKey
    @DynamoDbAttribute("driverId")
    public String getDriverId() {
        return driverId;
    }

    @DynamoDbSortKey
    @DynamoDbSecondarySortKey(indexNames = GEOHASH_GSI)
    @DynamoDbAttribute("timestamp")
    public Instant getTimestamp() {
        return timestamp;
    }

    @DynamoDbAttribute("latitude")
    public Double getLatitude() {
        return latitude;
    }

    @DynamoDbAttribute("longitude")
    public Double getLongitude() {
        return longitude;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = GEOHASH_GSI)
    @DynamoDbAttribute("geohash")
    public String getGeohash() {
        return geohash;
    }

    @DynamoDbAttribute("accuracy")
    public Double getAccuracy() {
        return accuracy;
    }

    @DynamoDbAttribute("speed")
    public Double getSpeed() {
        return speed;
    }

    @DynamoDbAttribute("heading")
    public Double getHeading() {
        return heading;
    }

    @DynamoDbAttribute("status")
    public String getStatus() {
        return status;
    }
}
