package com.fulfai.common.dynamodb;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.fulfai.common.dto.PaginatedResponse;

import io.quarkus.logging.Log;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException;

public class DynamoDBUtils {

    private static final int DEFAULT_PAGE_SIZE = 20;

    public static <T> T getItem(DynamoDbTable<T> table, String partitionKey) {
        Log.debugf("DYNAMODB_GET: table=%s, partitionKey=%s", table.tableName(), partitionKey);
        Key key = Key.builder().partitionValue(partitionKey).build();
        return table.getItem(GetItemEnhancedRequest.builder().key(key).build());
    }

    public static <T> T getItem(DynamoDbTable<T> table, String partitionKey, String sortKey) {
        Log.debugf("DYNAMODB_GET: table=%s, partitionKey=%s, sortKey=%s", table.tableName(), partitionKey, sortKey);
        Key key = Key.builder().partitionValue(partitionKey).sortValue(sortKey).build();
        return table.getItem(GetItemEnhancedRequest.builder().key(key).build());
    }

    @SuppressWarnings("unchecked")
    public static <T> void putItem(DynamoDbTable<T> table, T item) {
        Log.debugf("DYNAMODB_PUT: table=%s, item=%s", table.tableName(), item);
        PutItemEnhancedRequest<T> request = PutItemEnhancedRequest.builder((Class<T>) item.getClass())
                .item(item)
                .build();
        table.putItem(request);
    }

    public static <T> void deleteItem(DynamoDbTable<T> table, String partitionKey) {
        Log.debugf("DYNAMODB_DELETE: table=%s, partitionKey=%s", table.tableName(), partitionKey);
        Key key = Key.builder().partitionValue(partitionKey).build();
        table.deleteItem(DeleteItemEnhancedRequest.builder().key(key).build());
    }

    public static <T> void deleteItem(DynamoDbTable<T> table, String partitionKey, String sortKey) {
        Log.debugf("DYNAMODB_DELETE: table=%s, partitionKey=%s, sortKey=%s", table.tableName(), partitionKey, sortKey);
        Key key = Key.builder().partitionValue(partitionKey).sortValue(sortKey).build();
        table.deleteItem(DeleteItemEnhancedRequest.builder().key(key).build());
    }

    // Query by partition key with pagination
    public static <T> PaginatedResponse<T> queryByPartitionKey(DynamoDbTable<T> table, String partitionKey,
            String nextToken, Integer limit) {
        Log.debugf("DYNAMODB_QUERY: table=%s, partitionKey=%s, nextToken=%s, limit=%d",
                table.tableName(), partitionKey, nextToken, limit);

        int pageSize = limit != null ? limit : DEFAULT_PAGE_SIZE;
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(partitionKey).build()))
                .limit(pageSize);

        if (nextToken != null && !nextToken.isEmpty()) {
            requestBuilder.exclusiveStartKey(decodeExclusiveStartKey(nextToken));
        }

        return executeQuery(table, requestBuilder.build());
    }

    // Query by partition key with pagination (descending order by sort key)
    public static <T> PaginatedResponse<T> queryByPartitionKeyDescending(DynamoDbTable<T> table, String partitionKey,
            String nextToken, Integer limit) {
        Log.debugf("DYNAMODB_QUERY_DESC: table=%s, partitionKey=%s, nextToken=%s, limit=%d",
                table.tableName(), partitionKey, nextToken, limit);

        int pageSize = limit != null ? limit : DEFAULT_PAGE_SIZE;
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(partitionKey).build()))
                .scanIndexForward(false)
                .limit(pageSize);

        if (nextToken != null && !nextToken.isEmpty()) {
            requestBuilder.exclusiveStartKey(decodeExclusiveStartKey(nextToken));
        }

        return executeQuery(table, requestBuilder.build());
    }

    // Query by partition key and sort key begins with (with pagination)
    public static <T> PaginatedResponse<T> queryByPartitionKeyAndSortKeyBeginsWith(DynamoDbTable<T> table,
            String partitionKey, String sortKeyPrefix, String nextToken, Integer limit) {
        Log.debugf("DYNAMODB_QUERY: table=%s, partitionKey=%s, sortKeyPrefix=%s, nextToken=%s, limit=%d",
                table.tableName(), partitionKey, sortKeyPrefix, nextToken, limit);

        int pageSize = limit != null ? limit : DEFAULT_PAGE_SIZE;
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(Key.builder()
                        .partitionValue(partitionKey)
                        .sortValue(sortKeyPrefix)
                        .build()))
                .limit(pageSize);

        if (nextToken != null && !nextToken.isEmpty()) {
            requestBuilder.exclusiveStartKey(decodeExclusiveStartKey(nextToken));
        }

        return executeQuery(table, requestBuilder.build());
    }

    // Query by partition key and sort key between (with pagination)
    public static <T> PaginatedResponse<T> queryByPartitionKeyAndSortKeyBetween(DynamoDbTable<T> table,
            String partitionKey, String sortKeyStart, String sortKeyEnd, String nextToken, Integer limit) {
        Log.debugf("DYNAMODB_QUERY: table=%s, partitionKey=%s, sortKeyStart=%s, sortKeyEnd=%s, nextToken=%s, limit=%d",
                table.tableName(), partitionKey, sortKeyStart, sortKeyEnd, nextToken, limit);

        int pageSize = limit != null ? limit : DEFAULT_PAGE_SIZE;
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBetween(
                        Key.builder().partitionValue(partitionKey).sortValue(sortKeyStart).build(),
                        Key.builder().partitionValue(partitionKey).sortValue(sortKeyEnd).build()))
                .limit(pageSize);

        if (nextToken != null && !nextToken.isEmpty()) {
            requestBuilder.exclusiveStartKey(decodeExclusiveStartKey(nextToken));
        }

        return executeQuery(table, requestBuilder.build());
    }

    // Query GSI by partition key with pagination
    public static <T> PaginatedResponse<T> queryGsiByPartitionKey(DynamoDbIndex<T> index, String partitionKey,
            String nextToken, Integer limit) {
        Log.debugf("DYNAMODB_QUERY_GSI: index=%s, partitionKey=%s, nextToken=%s, limit=%d",
                index.indexName(), partitionKey, nextToken, limit);

        int pageSize = limit != null ? limit : DEFAULT_PAGE_SIZE;
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder().partitionValue(partitionKey).build()))
                .limit(pageSize);

        if (nextToken != null && !nextToken.isEmpty()) {
            requestBuilder.exclusiveStartKey(decodeExclusiveStartKey(nextToken));
        }

        return executeGsiQuery(index, requestBuilder.build());
    }

    // Query GSI by partition key and sort key (exact match)
    public static <T> PaginatedResponse<T> queryGsiByPartitionKeyAndSortKey(DynamoDbIndex<T> index,
            String partitionKey, String sortKey, String nextToken, Integer limit) {
        Log.debugf("DYNAMODB_QUERY_GSI: index=%s, partitionKey=%s, sortKey=%s, nextToken=%s, limit=%d",
                index.indexName(), partitionKey, sortKey, nextToken, limit);

        int pageSize = limit != null ? limit : DEFAULT_PAGE_SIZE;
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.keyEqualTo(Key.builder()
                        .partitionValue(partitionKey)
                        .sortValue(sortKey)
                        .build()))
                .limit(pageSize);

        if (nextToken != null && !nextToken.isEmpty()) {
            requestBuilder.exclusiveStartKey(decodeExclusiveStartKey(nextToken));
        }

        return executeGsiQuery(index, requestBuilder.build());
    }

    // Query GSI by partition key and sort key begins with (with pagination)
    public static <T> PaginatedResponse<T> queryGsiByPartitionKeyAndSortKeyBeginsWith(DynamoDbIndex<T> index,
            String partitionKey, String sortKeyPrefix, String nextToken, Integer limit) {
        Log.debugf("DYNAMODB_QUERY_GSI: index=%s, partitionKey=%s, sortKeyPrefix=%s, nextToken=%s, limit=%d",
                index.indexName(), partitionKey, sortKeyPrefix, nextToken, limit);

        int pageSize = limit != null ? limit : DEFAULT_PAGE_SIZE;
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBeginsWith(Key.builder()
                        .partitionValue(partitionKey)
                        .sortValue(sortKeyPrefix)
                        .build()))
                .limit(pageSize);

        if (nextToken != null && !nextToken.isEmpty()) {
            requestBuilder.exclusiveStartKey(decodeExclusiveStartKey(nextToken));
        }

        return executeGsiQuery(index, requestBuilder.build());
    }

    // Query GSI by partition key and sort key between (with pagination) - String sort keys
    public static <T> PaginatedResponse<T> queryGsiByPartitionKeyAndSortKeyBetween(DynamoDbIndex<T> index,
            String partitionKey, String sortKeyStart, String sortKeyEnd, String nextToken, Integer limit) {
        Log.debugf("DYNAMODB_QUERY_GSI: index=%s, partitionKey=%s, sortKeyStart=%s, sortKeyEnd=%s, nextToken=%s, limit=%d",
                index.indexName(), partitionKey, sortKeyStart, sortKeyEnd, nextToken, limit);

        int pageSize = limit != null ? limit : DEFAULT_PAGE_SIZE;
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBetween(
                        Key.builder().partitionValue(partitionKey).sortValue(sortKeyStart).build(),
                        Key.builder().partitionValue(partitionKey).sortValue(sortKeyEnd).build()))
                .limit(pageSize);

        if (nextToken != null && !nextToken.isEmpty()) {
            requestBuilder.exclusiveStartKey(decodeExclusiveStartKey(nextToken));
        }

        return executeGsiQuery(index, requestBuilder.build());
    }

    // Query GSI by partition key and sort key between (with pagination) - Instant sort keys
    public static <T> PaginatedResponse<T> queryGsiByPartitionKeyAndSortKeyBetween(DynamoDbIndex<T> index,
            String partitionKey, Instant sortKeyStart, Instant sortKeyEnd, String nextToken, Integer limit) {
        Log.debugf("DYNAMODB_QUERY_GSI: index=%s, partitionKey=%s, sortKeyStart=%s, sortKeyEnd=%s, nextToken=%s, limit=%d",
                index.indexName(), partitionKey, sortKeyStart, sortKeyEnd, nextToken, limit);

        int pageSize = limit != null ? limit : DEFAULT_PAGE_SIZE;
        QueryEnhancedRequest.Builder requestBuilder = QueryEnhancedRequest.builder()
                .queryConditional(QueryConditional.sortBetween(
                        Key.builder().partitionValue(partitionKey).sortValue(sortKeyStart.toString()).build(),
                        Key.builder().partitionValue(partitionKey).sortValue(sortKeyEnd.toString()).build()))
                .limit(pageSize);

        if (nextToken != null && !nextToken.isEmpty()) {
            requestBuilder.exclusiveStartKey(decodeExclusiveStartKey(nextToken));
        }

        return executeGsiQuery(index, requestBuilder.build());
    }

    // Execute query on table and return paginated response
    private static <T> PaginatedResponse<T> executeQuery(DynamoDbTable<T> table, QueryEnhancedRequest request) {
        List<T> items = new ArrayList<>();
        Map<String, AttributeValue> lastKey = null;

        for (Page<T> page : table.query(request)) {
            items.addAll(page.items());
            lastKey = page.lastEvaluatedKey();
            break; // Only get one page
        }

        String nextTokenResult = encodeLastEvaluatedKey(lastKey);
        Log.debugf("DYNAMODB_QUERY_RESULT: table=%s, count=%d, hasMore=%b",
                table.tableName(), items.size(), nextTokenResult != null);

        return PaginatedResponse.<T>builder()
                .items(items)
                .nextToken(nextTokenResult)
                .hasMore(nextTokenResult != null)
                .build();
    }

    // Execute query on GSI and return paginated response
    private static <T> PaginatedResponse<T> executeGsiQuery(DynamoDbIndex<T> index, QueryEnhancedRequest request) {
        List<T> items = new ArrayList<>();
        Map<String, AttributeValue> lastKey = null;

        for (Page<T> page : index.query(request)) {
            items.addAll(page.items());
            lastKey = page.lastEvaluatedKey();
            break; // Only get one page
        }

        String nextTokenResult = encodeLastEvaluatedKey(lastKey);
        Log.debugf("DYNAMODB_QUERY_GSI_RESULT: index=%s, count=%d, hasMore=%b",
                index.indexName(), items.size(), nextTokenResult != null);

        return PaginatedResponse.<T>builder()
                .items(items)
                .nextToken(nextTokenResult)
                .hasMore(nextTokenResult != null)
                .build();
    }

    // Encode last evaluated key to base64 string for pagination token
    private static String encodeLastEvaluatedKey(Map<String, AttributeValue> lastEvaluatedKey) {
        if (lastEvaluatedKey == null || lastEvaluatedKey.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, AttributeValue> entry : lastEvaluatedKey.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue().s());
        }
        return Base64.getUrlEncoder().encodeToString(sb.toString().getBytes());
    }

    // Decode pagination token to exclusive start key
    private static Map<String, AttributeValue> decodeExclusiveStartKey(String nextToken) {
        if (nextToken == null || nextToken.isEmpty()) {
            return null;
        }

        try {
            String decoded = new String(Base64.getUrlDecoder().decode(nextToken));
            java.util.HashMap<String, AttributeValue> key = new java.util.HashMap<>();
            for (String pair : decoded.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    key.put(kv[0], AttributeValue.builder().s(kv[1]).build());
                }
            }
            return key;
        } catch (Exception e) {
            Log.warnf("Failed to decode pagination token: %s", e.getMessage());
            return null;
        }
    }

    // ==================== Transaction Support ====================

    /**
     * Execute transactWriteItems with the provided builder consumer.
     * @param enhancedClient The DynamoDB enhanced client
     * @param requestBuilder Consumer to build the transaction request
     */
    public static void transactWriteItems(DynamoDbEnhancedClient enhancedClient,
            Consumer<TransactWriteItemsEnhancedRequest.Builder> requestBuilder) {
        Log.debug("DYNAMODB_TRANSACT_WRITE: Starting transaction");
        try {
            TransactWriteItemsEnhancedRequest.Builder builder = TransactWriteItemsEnhancedRequest.builder();
            requestBuilder.accept(builder);
            enhancedClient.transactWriteItems(builder.build());
            Log.debug("DYNAMODB_TRANSACT_WRITE: Transaction completed successfully");
        } catch (TransactionCanceledException e) {
            Log.errorf("DYNAMODB_TRANSACT_WRITE: Transaction cancelled - reasons: %s", e.cancellationReasons());
            throw new TransactionFailedException("Transaction cancelled: " + extractCancellationReasons(e), e);
        }
    }

    /**
     * Put item with condition expression (for conditional writes in transactions).
     * @param table The DynamoDB table
     * @param item The item to put
     * @param conditionExpression The condition expression
     */
    @SuppressWarnings("unchecked")
    public static <T> void putItemWithCondition(DynamoDbTable<T> table, T item, Expression conditionExpression) {
        Log.debugf("DYNAMODB_PUT_CONDITIONAL: table=%s, item=%s", table.tableName(), item);
        PutItemEnhancedRequest<T> request = PutItemEnhancedRequest.builder((Class<T>) item.getClass())
                .item(item)
                .conditionExpression(conditionExpression)
                .build();
        table.putItem(request);
    }

    /**
     * Update item with condition expression.
     * @param table The DynamoDB table
     * @param item The item to update
     * @param itemClass The class of the item
     * @param conditionExpression The condition expression
     */
    @SuppressWarnings("unchecked")
    public static <T> void updateItemWithCondition(DynamoDbTable<T> table, T item, Class<T> itemClass, Expression conditionExpression) {
        Log.debugf("DYNAMODB_UPDATE_CONDITIONAL: table=%s, item=%s", table.tableName(), item);
        table.updateItem(UpdateItemEnhancedRequest.builder(itemClass)
                .item(item)
                .conditionExpression(conditionExpression)
                .build());
    }

    /**
     * Create a condition expression to check if an attribute equals a specific value.
     * @param attributeName The attribute name
     * @param expectedValue The expected value
     * @return Expression for condition check
     */
    public static Expression attributeEquals(String attributeName, String expectedValue) {
        return Expression.builder()
                .expression("#attr = :val")
                .putExpressionName("#attr", attributeName)
                .putExpressionValue(":val", AttributeValue.builder().s(expectedValue).build())
                .build();
    }

    /**
     * Create a condition expression to check if an attribute is in a list of values.
     * @param attributeName The attribute name
     * @param allowedValues The allowed values
     * @return Expression for condition check
     */
    public static Expression attributeIn(String attributeName, List<String> allowedValues) {
        Expression.Builder builder = Expression.builder();
        StringBuilder expression = new StringBuilder("#attr IN (");
        Map<String, AttributeValue> values = new java.util.HashMap<>();

        for (int i = 0; i < allowedValues.size(); i++) {
            if (i > 0) {
                expression.append(", ");
            }
            String placeholder = ":val" + i;
            expression.append(placeholder);
            values.put(placeholder, AttributeValue.builder().s(allowedValues.get(i)).build());
        }
        expression.append(")");

        builder.expression(expression.toString())
                .putExpressionName("#attr", attributeName)
                .expressionValues(values);

        return builder.build();
    }

    /**
     * Create a condition expression to check stock quantity is sufficient.
     * @param requiredQuantity The minimum quantity required
     * @return Expression for condition check
     */
    public static Expression stockQuantitySufficient(int requiredQuantity) {
        return Expression.builder()
                .expression("stockQuantity >= :requiredQty")
                .putExpressionValue(":requiredQty", AttributeValue.builder().n(String.valueOf(requiredQuantity)).build())
                .build();
    }

    /**
     * Create a condition expression to check if an attribute exists (item exists).
     * @param attributeName The attribute name to check for existence
     * @return Expression for condition check
     */
    public static Expression attributeExists(String attributeName) {
        return Expression.builder()
                .expression("attribute_exists(#attr)")
                .putExpressionName("#attr", attributeName)
                .build();
    }

    /**
     * Create a combined condition: attribute exists AND equals expected value.
     * Useful for checking item exists with specific status.
     */
    public static Expression attributeExistsAndEquals(String attributeName, String expectedValue) {
        return Expression.builder()
                .expression("attribute_exists(#attr) AND #attr = :val")
                .putExpressionName("#attr", attributeName)
                .putExpressionValue(":val", AttributeValue.builder().s(expectedValue).build())
                .build();
    }

    /**
     * Extract human-readable cancellation reasons from TransactionCanceledException.
     */
    private static String extractCancellationReasons(TransactionCanceledException e) {
        if (e.cancellationReasons() == null || e.cancellationReasons().isEmpty()) {
            return "Unknown reason";
        }
        return e.cancellationReasons().stream()
                .filter(r -> r.code() != null && !"None".equals(r.code()))
                .map(r -> r.code() + ": " + (r.message() != null ? r.message() : "No message"))
                .reduce((a, b) -> a + "; " + b)
                .orElse("No specific reason");
    }

    /**
     * Custom exception for transaction failures.
     */
    public static class TransactionFailedException extends RuntimeException {
        public TransactionFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
