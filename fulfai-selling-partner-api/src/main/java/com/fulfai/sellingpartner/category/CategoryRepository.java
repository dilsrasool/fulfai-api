package com.fulfai.sellingpartner.category;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.sellingpartner.Schemas;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@ApplicationScoped
public class CategoryRepository {

    @ConfigProperty(name = "category.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    // --------------------------------------------------
    // Table
    // --------------------------------------------------
    private DynamoDbTable<Category> getTable() {
        return clientFactory
                .getEnhancedDynamoClient()
                .table(tableName, Schemas.CATEGORY_SCHEMA);
    }

    // --------------------------------------------------
    // GSI: parent-index (PK = parentCategoryId, SK = categoryId)
    // --------------------------------------------------
    private DynamoDbIndex<Category> getParentIndex() {
        return getTable().index(Category.PARENT_GSI);
    }

    // --------------------------------------------------
    // Get by company + categoryId (PRIMARY KEY LOOKUP)
    // --------------------------------------------------
    public Category getByCompanyAndId(String companyId, String categoryId) {
        return DynamoDBUtils.getItem(
                getTable(),
                companyId,
                categoryId
        );
    }

    // --------------------------------------------------
    // Get by company + name (uniqueness check)
    // --------------------------------------------------
    public Category getByCompanyAndName(String companyId, String name) {
        return DynamoDBUtils
                .queryByPartitionKey(
                        getTable(),
                        companyId,
                        null,
                        null
                )
                .getItems()
                .stream()
                .filter(c -> name.equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElse(null);
    }

    // --------------------------------------------------
    // Get all categories for a company
    // --------------------------------------------------
    public List<Category> getAllByCompany(String companyId) {
        return DynamoDBUtils
                .queryByPartitionKey(
                        getTable(),
                        companyId,
                        null,
                        null
                )
                .getItems();
    }

    // --------------------------------------------------
    // Get children categories (hierarchy)
    // --------------------------------------------------
    public List<Category> getChildren(String parentCategoryId) {
        return DynamoDBUtils
                .queryGsiByPartitionKey(
                        getParentIndex(),
                        parentCategoryId,
                        null,
                        null
                )
                .getItems();
    }

    // --------------------------------------------------
    // Save (create / update)
    // --------------------------------------------------
    public void save(Category category) {
        DynamoDBUtils.putItem(getTable(), category);
    }

    // --------------------------------------------------
    // Delete (company-safe)
    // --------------------------------------------------
    public void delete(String companyId, String categoryId) {
        DynamoDBUtils.deleteItem(
                getTable(),
                companyId,
                categoryId
        );
    }
}
