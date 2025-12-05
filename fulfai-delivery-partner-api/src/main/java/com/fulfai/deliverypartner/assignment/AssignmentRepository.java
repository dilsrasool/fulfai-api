package com.fulfai.deliverypartner.assignment;

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
public class AssignmentRepository {

    private final DynamoDbTable<DriverOrderAssignment> assignmentTable;
    private final DynamoDbIndex<DriverOrderAssignment> orderIndex;
    private final DynamoDbIndex<DriverOrderAssignment> statusIndex;

    @Inject
    public AssignmentRepository(ClientFactory clientFactory,
            @ConfigProperty(name = "delivery.assignment.table.name") String tableName) {
        DynamoDbEnhancedClient enhancedClient = clientFactory.getEnhancedDynamoClient();
        this.assignmentTable = enhancedClient.table(tableName, Schemas.ASSIGNMENT_SCHEMA);
        this.orderIndex = assignmentTable.index(DriverOrderAssignment.ORDER_GSI);
        this.statusIndex = assignmentTable.index(DriverOrderAssignment.STATUS_GSI);
    }

    public PaginatedResponse<DriverOrderAssignment> getByDriver(String driverId, String nextToken, Integer limit) {
        return DynamoDBUtils.queryByPartitionKeyDescending(assignmentTable, driverId, nextToken, limit);
    }

    public PaginatedResponse<DriverOrderAssignment> getByOrder(String orderId, String nextToken, Integer limit) {
        return DynamoDBUtils.queryGsiByPartitionKey(orderIndex, orderId, nextToken, limit);
    }

    public PaginatedResponse<DriverOrderAssignment> getByStatus(String status, String nextToken, Integer limit) {
        return DynamoDBUtils.queryGsiByPartitionKey(statusIndex, status, nextToken, limit);
    }

    public void save(DriverOrderAssignment assignment) {
        DynamoDBUtils.putItem(assignmentTable, assignment);
    }

    public DynamoDbTable<DriverOrderAssignment> getAssignmentTable() {
        return assignmentTable;
    }
}
