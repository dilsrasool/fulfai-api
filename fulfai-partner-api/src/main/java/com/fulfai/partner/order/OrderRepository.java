package com.fulfai.partner.order;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.partner.Schemas;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@ApplicationScoped
@RegisterForReflection
public class OrderRepository {

    @ConfigProperty(name = "order.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    private DynamoDbTable<Order> getOrderTable() {
        return clientFactory.getEnhancedDynamoClient().table(tableName, Schemas.ORDER_SCHEMA);
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

    public PaginatedResponse<Order> getByDate(String companyId, String date, String nextToken, Integer limit) {
        return DynamoDBUtils.queryGsiByPartitionKeyAndSortKeyBeginsWith(
                getDateStatusIndex(), companyId, date, nextToken, limit);
    }

    public PaginatedResponse<Order> getByDateRange(String companyId, String startDate, String endDate,
            String nextToken, Integer limit) {
        return DynamoDBUtils.queryGsiByPartitionKeyAndSortKeyBetween(
                getDateStatusIndex(), companyId, startDate, endDate + "~", nextToken, limit);
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
}
