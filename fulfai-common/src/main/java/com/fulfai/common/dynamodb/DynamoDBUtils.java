package com.fulfai.common.dynamodb;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.fulfai.common.dto.PaginatedResponse;

import io.quarkus.logging.Log;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class DynamoDBUtils {

    private static final int DEFAULT_PAGE_SIZE = 20;

    public static <T> T getItem(DynamoDbTable<T> table, String partitionKey) {
        Log.debugf("DYNAMODB_GET: table=%s, partitionKey=%s", table.tableName(), partitionKey);
        return table.getItem(r -> r.key(k -> k.partitionValue(partitionKey)));
    }

    public static <T> T getItem(DynamoDbTable<T> table, String partitionKey, String sortKey) {
        Log.debugf("DYNAMODB_GET: table=%s, partitionKey=%s, sortKey=%s", table.tableName(), partitionKey, sortKey);
        return table.getItem(r -> r.key(k -> k.partitionValue(partitionKey).sortValue(sortKey)));
    }

    public static <T> void putItem(DynamoDbTable<T> table, T item) {
        Log.debugf("DYNAMODB_PUT: table=%s, item=%s", table.tableName(), item);
        table.putItem(item);
    }

    public static <T> void deleteItem(DynamoDbTable<T> table, String partitionKey) {
        Log.debugf("DYNAMODB_DELETE: table=%s, partitionKey=%s", table.tableName(), partitionKey);
        table.deleteItem(r -> r.key(k -> k.partitionValue(partitionKey)));
    }

    public static <T> void deleteItem(DynamoDbTable<T> table, String partitionKey, String sortKey) {
        Log.debugf("DYNAMODB_DELETE: table=%s, partitionKey=%s, sortKey=%s", table.tableName(), partitionKey, sortKey);
        table.deleteItem(r -> r.key(k -> k.partitionValue(partitionKey).sortValue(sortKey)));
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

    // Query GSI by partition key and sort key between (with pagination)
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
}
