package com.fulfai.sellingpartner.company;

import java.util.List;
import java.util.ArrayList;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.sellingpartner.Schemas;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@ApplicationScoped
@RegisterForReflection
public class CompanyRepository {

    @ConfigProperty(name = "company.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    private DynamoDbTable<Company> getCompanyTable() {
        return clientFactory
                .getEnhancedDynamoClient()
                .table(tableName, Schemas.COMPANY_SCHEMA);
    }

    public Company getById(String id) {
        return DynamoDBUtils.getItem(getCompanyTable(), id);
    }

    public void save(Company company) {
        DynamoDBUtils.putItem(getCompanyTable(), company);
    }

    public void delete(String id) {
        DynamoDBUtils.deleteItem(getCompanyTable(), id);
    }

    public String getTableName() {
        return tableName;
    }

    /**
     * ✅ Get ALL companies for a given ownerSub (GSI: ownerSub-index)
     */
    public List<Company> getAllByOwnerSub(String ownerSub) {
        var index = getCompanyTable().index("ownerSub-index");

        var queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(ownerSub).build()
        );

        List<Company> results = new ArrayList<>();

        index.query(queryConditional).forEach(page -> {
            page.items().forEach(results::add);
        });

        return results;
    }

    /**
     * ✅ Get first company (legacy behavior)
     */
    public Company getByOwnerSub(String ownerSub) {
        List<Company> companies = getAllByOwnerSub(ownerSub);
        return companies.isEmpty() ? null : companies.get(0);
    }
}
