package com.fulfai.deliverypartner.company;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.deliverypartner.Schemas;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@ApplicationScoped
public class CompanyRepository {

    private final DynamoDbTable<Company> companyTable;

    @Inject
    public CompanyRepository(ClientFactory clientFactory,
            @ConfigProperty(name = "delivery.company.table.name") String tableName) {
        DynamoDbEnhancedClient enhancedClient = clientFactory.getEnhancedDynamoClient();
        this.companyTable = enhancedClient.table(tableName, Schemas.COMPANY_SCHEMA);
    }

    public Company getById(String id) {
        return DynamoDBUtils.getItem(companyTable, id);
    }

    public void save(Company company) {
        DynamoDBUtils.putItem(companyTable, company);
    }

    public void delete(String id) {
        DynamoDBUtils.deleteItem(companyTable, id);
    }
}
