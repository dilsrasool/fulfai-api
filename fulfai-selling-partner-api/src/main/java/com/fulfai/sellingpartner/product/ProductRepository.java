package com.fulfai.sellingpartner.product;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.Schemas;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;

@ApplicationScoped
@RegisterForReflection
public class ProductRepository {

    @ConfigProperty(name = "product.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    private DynamoDbEnhancedClient enhancedClient() {
        return clientFactory.getEnhancedDynamoClient();
    }

    public DynamoDbTable<Product> getProductTable() {
        return enhancedClient().table(tableName, Schemas.PRODUCT_SCHEMA);
    }

    private DynamoDbIndex<Product> getCategoryIndex() {
        return getProductTable().index(Product.CATEGORY_GSI);
    }

    public Product getById(String companyId, String branchId, String productId) {
        String branchProductKey = branchId + "#" + productId;
        return DynamoDBUtils.getItem(getProductTable(), companyId, branchProductKey);
    }

    public PaginatedResponse<Product> getByCompanyId(String companyId, String nextToken, Integer limit) {
        return DynamoDBUtils.queryByPartitionKey(getProductTable(), companyId, nextToken, limit);
    }

    public PaginatedResponse<Product> scanAll(String nextToken, Integer limit) {
        return DynamoDBUtils.scan(getProductTable(), nextToken, limit);
    }

    public PaginatedResponse<Product> getByBranch(String companyId, String branchId, String nextToken, Integer limit) {
        return DynamoDBUtils.queryByPartitionKeyAndSortKeyBeginsWith(
                getProductTable(), companyId, branchId + "#", nextToken, limit);
    }

        public PaginatedResponse<Product> getByBranchAndKeyword(
            String companyId,
            String branchId,
            String keyword,
            String nextToken,
            Integer limit
        ) {
        Expression filterExpression = Expression.builder()
            .expression("contains(#name, :keyword) OR contains(#description, :keyword)")
            .putExpressionName("#name", "name")
            .putExpressionName("#description", "description")
            .putExpressionValue(":keyword", AttributeValue.builder().s(keyword).build())
            .build();

        return DynamoDBUtils.queryByPartitionKeyAndSortKeyBeginsWith(
            getProductTable(),
            companyId,
            branchId + "#",
            filterExpression,
            nextToken,
            limit
        );
        }

    public PaginatedResponse<Product> getByCategory(String category, String nextToken, Integer limit) {
        return DynamoDBUtils.queryGsiByPartitionKey(getCategoryIndex(), category, nextToken, limit);
    }

    public PaginatedResponse<Product> getByCategoryAndCompany(String category, String companyId,
            String nextToken, Integer limit) {
        return DynamoDBUtils.queryGsiByPartitionKeyAndSortKey(
                getCategoryIndex(), category, companyId, nextToken, limit);
    }

    public void save(Product product) {
        DynamoDBUtils.putItem(getProductTable(), product);
    }

    public void delete(String companyId, String branchId, String productId) {
        String branchProductKey = branchId + "#" + productId;
        DynamoDBUtils.deleteItem(getProductTable(), companyId, branchProductKey);
    }

    // ✅ NEW: Batch save for CSV upload
    public void batchSave(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        final int BATCH_SIZE = 25; // DynamoDB batch write limit

        for (int i = 0; i < products.size(); i += BATCH_SIZE) {
            List<Product> chunk = products.subList(i, Math.min(i + BATCH_SIZE, products.size()));
            batchWriteChunk(chunk);
        }
    }

    private void batchWriteChunk(List<Product> chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }

        DynamoDbTable<Product> table = getProductTable();

        WriteBatch.Builder<Product> writeBatchBuilder = WriteBatch.builder(Product.class)
                .mappedTableResource(table);

        for (Product p : chunk) {
            writeBatchBuilder.addPutItem(r -> r.item(p));
        }

        BatchWriteItemEnhancedRequest request = BatchWriteItemEnhancedRequest.builder()
                .writeBatches(writeBatchBuilder.build())
                .build();

        int maxRetries = 3;
        int attempt = 0;

        while (true) {
            attempt++;

            var result = enhancedClient().batchWriteItem(request);

            List<Product> unprocessed = result.unprocessedPutItemsForTable(table);

            if (unprocessed == null || unprocessed.isEmpty()) {
                return;
            }

            if (attempt >= maxRetries) {
                throw new RuntimeException(
                        "Failed to batch write products after retries. Unprocessed items count: " + unprocessed.size());
            }

            // Retry only unprocessed items
            WriteBatch.Builder<Product> retryBatch = WriteBatch.builder(Product.class)
                    .mappedTableResource(table);

            for (Product p : unprocessed) {
                retryBatch.addPutItem(r -> r.item(p));
            }

            request = BatchWriteItemEnhancedRequest.builder()
                    .writeBatches(retryBatch.build())
                    .build();
        }
    }

    public String getTableName() {
        return tableName;
    }
}
