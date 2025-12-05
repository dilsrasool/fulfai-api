package com.fulfai.deliverypartner.location;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.deliverypartner.Schemas;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@ApplicationScoped
public class LocationRepository {

    private final DynamoDbTable<DriverLocation> locationTable;
    private final DynamoDbIndex<DriverLocation> geohashIndex;

    @Inject
    public LocationRepository(ClientFactory clientFactory,
            @ConfigProperty(name = "delivery.location.table.name") String tableName) {
        DynamoDbEnhancedClient enhancedClient = clientFactory.getEnhancedDynamoClient();
        this.locationTable = enhancedClient.table(tableName, Schemas.LOCATION_SCHEMA);
        this.geohashIndex = locationTable.index(DriverLocation.GEOHASH_GSI);
    }

    public void save(DriverLocation location) {
        DynamoDBUtils.putItem(locationTable, location);
    }

    public PaginatedResponse<DriverLocation> getByDriver(String driverId, String nextToken, Integer limit) {
        return DynamoDBUtils.queryByPartitionKeyDescending(locationTable, driverId, nextToken, limit);
    }

    public PaginatedResponse<DriverLocation> getByGeohash(String geohash, String nextToken, Integer limit) {
        return DynamoDBUtils.queryGsiByPartitionKey(geohashIndex, geohash, nextToken, limit);
    }

    /**
     * Query multiple geohashes (for proximity search).
     * Returns combined results from all geohash queries.
     */
    public List<DriverLocation> getByGeohashes(List<String> geohashes, Integer limitPerHash) {
        return geohashes.stream()
                .flatMap(hash -> {
                    PaginatedResponse<DriverLocation> response = getByGeohash(hash, null, limitPerHash);
                    return response.getItems().stream();
                })
                .toList();
    }
}
