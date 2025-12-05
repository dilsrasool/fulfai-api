package com.fulfai.partner.category;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.partner.Schemas;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@ApplicationScoped
@RegisterForReflection
public class CategoryRepository {

    @ConfigProperty(name = "category.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    private DynamoDbTable<Category> getCategoryTable() {
        return clientFactory.getEnhancedDynamoClient().table(tableName, Schemas.CATEGORY_SCHEMA);
    }

    public Category getById(String companyId, String name) {
        return DynamoDBUtils.getItem(getCategoryTable(), companyId, name);
    }

    public PaginatedResponse<Category> getByCompanyId(String companyId, String nextToken, Integer limit) {
        return DynamoDBUtils.queryByPartitionKey(getCategoryTable(), companyId, nextToken, limit);
    }

    public void save(Category category) {
        DynamoDBUtils.putItem(getCategoryTable(), category);
    }

    public void delete(String companyId, String name) {
        DynamoDBUtils.deleteItem(getCategoryTable(), companyId, name);
    }

    public String getTableName() {
        return tableName;
    }
}
