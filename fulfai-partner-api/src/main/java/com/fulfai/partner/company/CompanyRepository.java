package com.fulfai.partner.company;

import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.partner.Schemas;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@ApplicationScoped
@RegisterForReflection
public class CompanyRepository {

    @ConfigProperty(name = "company.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    private DynamoDbTable<Company> getCompanyTable() {
        return clientFactory.getEnhancedDynamoClient().table(tableName, Schemas.COMPANY_SCHEMA);
    }

    public Company getById(String id) {
        return DynamoDBUtils.getItem(getCompanyTable(), id);
    }

    public List<Company> getAll() {
        return DynamoDBUtils.scan(getCompanyTable());
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
}
