package com.fulfai.partner.order;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.partner.Schemas;

import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

@ApplicationScoped
@RegisterForReflection
public class OrderRepository {

    @ConfigProperty(name = "order.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    @Inject
    DynamoDbClient dynamoDbClient;

    public DynamoDbTable<Order> getOrderTable() {
        return clientFactory.getEnhancedDynamoClient().table(tableName, Schemas.ORDER_SCHEMA);
    }

    public DynamoDbEnhancedClient getEnhancedClient() {
        return clientFactory.getEnhancedDynamoClient();
    }

    private DynamoDbIndex<Order> getDateStatusIndex() {
        return getOrderTable().index(Order.DATE_STATUS_GSI);
    }

    public Order getById(String companyId, String orderId) {
        return DynamoDBUtils.getItem(getOrderTable(), companyId, orderId);
    }

    public PaginatedResponse<Order> getByCompanyId(String companyId, String nextToken, Integer limit) {
        return DynamoDBUtils.queryByPartitionKey(getOrderTable(), companyId, nextToken, limit);
    }

    public PaginatedResponse<Order> getByDateAndStatus(String companyId, String date, String status,
            String nextToken, Integer limit) {
        String sortKeyPrefix = date + "#" + status;
        return DynamoDBUtils.queryGsiByPartitionKeyAndSortKeyBeginsWith(
                getDateStatusIndex(), companyId, sortKeyPrefix, nextToken, limit);
    }

    public PaginatedResponse<Order> getByDateRange(String companyId, Instant startDate, Instant endDate,
            String nextToken, Integer limit) {
        // Use ISO timestamp format for range queries
        // startDate: timestamp# to include all statuses from start
        // endDate: timestamp#~ to include all statuses up to and including end
        return DynamoDBUtils.queryGsiByPartitionKeyAndSortKeyBetween(
                getDateStatusIndex(), companyId, startDate.toString() + "#", endDate.toString() + "#~", nextToken, limit);
    }

    public void save(Order order) {
        DynamoDBUtils.putItem(getOrderTable(), order);
    }

    public void delete(String companyId, String orderId) {
        DynamoDBUtils.deleteItem(getOrderTable(), companyId, orderId);
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * Update order status using conditional update.
     * Condition: item exists AND current status is in the allowed list.
     * This avoids fetching the order before updating.
     */
    public void updateStatus(String companyId, String orderId, String newStatus, List<String> allowedFromStatuses) {
        Log.debugf("DYNAMODB_UPDATE_STATUS: companyId=%s, orderId=%s, newStatus=%s, allowedFrom=%s",
                companyId, orderId, newStatus, allowedFromStatuses);

        // Build condition expression for allowed statuses
        StringBuilder conditionExpr = new StringBuilder("attribute_exists(companyId) AND (");
        Map<String, AttributeValue> expressionValues = new HashMap<>();

        for (int i = 0; i < allowedFromStatuses.size(); i++) {
            if (i > 0) conditionExpr.append(" OR ");
            String placeholder = ":status" + i;
            conditionExpr.append("#status = ").append(placeholder);
            expressionValues.put(placeholder, AttributeValue.builder().s(allowedFromStatuses.get(i)).build());
        }
        conditionExpr.append(")");

        // Add update expression values
        expressionValues.put(":newStatus", AttributeValue.builder().s(newStatus).build());
        expressionValues.put(":newDateStatusKey", AttributeValue.builder().s("STATUS#" + newStatus).build());
        expressionValues.put(":updatedAt", AttributeValue.builder().s(Instant.now().toString()).build());

        Map<String, String> expressionNames = new HashMap<>();
        expressionNames.put("#status", "status");
        expressionNames.put("#dateStatusKey", "dateStatusKey");
        expressionNames.put("#updatedAt", "updatedAt");

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(Map.of(
                        "companyId", AttributeValue.builder().s(companyId).build(),
                        "orderId", AttributeValue.builder().s(orderId).build()))
                .updateExpression("SET #status = :newStatus, #dateStatusKey = :newDateStatusKey, #updatedAt = :updatedAt")
                .conditionExpression(conditionExpr.toString())
                .expressionAttributeNames(expressionNames)
                .expressionAttributeValues(expressionValues)
                .build();

        dynamoDbClient.updateItem(request);
    }
}
