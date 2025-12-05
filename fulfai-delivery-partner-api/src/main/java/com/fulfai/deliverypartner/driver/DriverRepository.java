package com.fulfai.deliverypartner.driver;

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
public class DriverRepository {

    private final DynamoDbTable<Driver> driverTable;
    private final DynamoDbIndex<Driver> statusIndex;

    @Inject
    public DriverRepository(ClientFactory clientFactory,
            @ConfigProperty(name = "delivery.driver.table.name") String tableName) {
        DynamoDbEnhancedClient enhancedClient = clientFactory.getEnhancedDynamoClient();
        this.driverTable = enhancedClient.table(tableName, Schemas.DRIVER_SCHEMA);
        this.statusIndex = driverTable.index(Driver.STATUS_GSI);
    }

    public Driver getById(String companyId, String driverId) {
        return DynamoDBUtils.getItem(driverTable, companyId, driverId);
    }

    public PaginatedResponse<Driver> getByCompany(String companyId, String nextToken, Integer limit) {
        return DynamoDBUtils.queryByPartitionKey(driverTable, companyId, nextToken, limit);
    }

    public PaginatedResponse<Driver> getByStatus(String status, String nextToken, Integer limit) {
        return DynamoDBUtils.queryGsiByPartitionKey(statusIndex, status, nextToken, limit);
    }

    public void save(Driver driver) {
        DynamoDBUtils.putItem(driverTable, driver);
    }

    public void delete(String companyId, String driverId) {
        DynamoDBUtils.deleteItem(driverTable, companyId, driverId);
    }

    public DynamoDbTable<Driver> getDriverTable() {
        return driverTable;
    }
}
