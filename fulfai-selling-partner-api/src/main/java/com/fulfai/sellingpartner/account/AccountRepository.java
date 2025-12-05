package com.fulfai.sellingpartner.account;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.Schemas;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@ApplicationScoped
@RegisterForReflection
public class AccountRepository {

    @ConfigProperty(name = "account.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    public DynamoDbTable<Account> getAccountTable() {
        return clientFactory.getEnhancedDynamoClient().table(tableName, Schemas.ACCOUNT_SCHEMA);
    }

    public PaginatedResponse<Account> getByCompanyAndAccount(String companyId, String accountName,
            String nextToken, Integer limit) {
        String companyAccountKey = Account.buildCompanyAccountKey(companyId, accountName);
        return DynamoDBUtils.queryByPartitionKey(getAccountTable(), companyAccountKey, nextToken, limit);
    }

    public Account getLatestByCompanyAndAccount(String companyId, String accountName) {
        String companyAccountKey = Account.buildCompanyAccountKey(companyId, accountName);
        PaginatedResponse<Account> response = DynamoDBUtils.queryByPartitionKeyDescending(
                getAccountTable(), companyAccountKey, null, 1);
        if (response.getItems() != null && !response.getItems().isEmpty()) {
            return response.getItems().get(0);
        }
        return null;
    }

    public void save(Account account) {
        DynamoDBUtils.putItem(getAccountTable(), account);
    }

    public String getTableName() {
        return tableName;
    }
}
