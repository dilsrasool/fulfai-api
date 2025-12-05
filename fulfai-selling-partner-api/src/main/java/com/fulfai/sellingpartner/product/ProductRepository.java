package com.fulfai.sellingpartner.product;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.Schemas;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@ApplicationScoped
@RegisterForReflection
public class ProductRepository {

    @ConfigProperty(name = "product.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    public DynamoDbTable<Product> getProductTable() {
        return clientFactory.getEnhancedDynamoClient().table(tableName, Schemas.PRODUCT_SCHEMA);
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

    public PaginatedResponse<Product> getByBranch(String companyId, String branchId, String nextToken, Integer limit) {
        return DynamoDBUtils.queryByPartitionKeyAndSortKeyBeginsWith(
                getProductTable(), companyId, branchId + "#", nextToken, limit);
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

    public String getTableName() {
        return tableName;
    }
}
