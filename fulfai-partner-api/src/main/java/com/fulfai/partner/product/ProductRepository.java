package com.fulfai.partner.product;

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
public class ProductRepository {

    @ConfigProperty(name = "product.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    private DynamoDbTable<Product> getProductTable() {
        return clientFactory.getEnhancedDynamoClient().table(tableName, Schemas.PRODUCT_SCHEMA);
    }

    private DynamoDbIndex<Product> getCategoryIndex() {
        return getProductTable().index(Product.CATEGORY_GSI);
    }

    public Product getById(String companyId, String productId) {
        return DynamoDBUtils.getItem(getProductTable(), companyId, productId);
    }

    public PaginatedResponse<Product> getByCompanyId(String companyId, String nextToken, Integer limit) {
        return DynamoDBUtils.queryByPartitionKey(getProductTable(), companyId, nextToken, limit);
    }

    public PaginatedResponse<Product> getByCategory(String companyId, String category, String nextToken, Integer limit) {
        return DynamoDBUtils.queryGsiByPartitionKeyAndSortKeyBeginsWith(
                getCategoryIndex(), companyId, category, nextToken, limit);
    }

    public void save(Product product) {
        DynamoDBUtils.putItem(getProductTable(), product);
    }

    public void delete(String companyId, String productId) {
        DynamoDBUtils.deleteItem(getProductTable(), companyId, productId);
    }

    public String getTableName() {
        return tableName;
    }
}
