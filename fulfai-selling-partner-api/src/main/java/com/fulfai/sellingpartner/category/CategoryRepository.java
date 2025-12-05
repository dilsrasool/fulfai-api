package com.fulfai.sellingpartner.category;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.Schemas;

import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;

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

    private DynamoDbIndex<Category> getParentIndex() {
        return getCategoryTable().index(Category.PARENT_GSI);
    }

    public Category getByName(String name) {
        return DynamoDBUtils.getItem(getCategoryTable(), name);
    }

    public List<Category> getAll() {
        Log.debugf("DYNAMODB_SCAN: table=%s", tableName);
        List<Category> items = new ArrayList<>();
        for (Page<Category> page : getCategoryTable().scan(ScanEnhancedRequest.builder().build())) {
            items.addAll(page.items());
        }
        Log.debugf("DYNAMODB_SCAN_RESULT: table=%s, count=%d", tableName, items.size());
        return items;
    }

    public PaginatedResponse<Category> getByParentCategory(String parentCategory, String nextToken, Integer limit) {
        return DynamoDBUtils.queryGsiByPartitionKey(getParentIndex(), parentCategory, nextToken, limit);
    }

    public void save(Category category) {
        DynamoDBUtils.putItem(getCategoryTable(), category);
    }

    public void delete(String name) {
        DynamoDBUtils.deleteItem(getCategoryTable(), name);
    }

    public String getTableName() {
        return tableName;
    }
}
