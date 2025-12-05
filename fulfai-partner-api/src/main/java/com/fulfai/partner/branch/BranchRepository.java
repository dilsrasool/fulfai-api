package com.fulfai.partner.branch;

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
public class BranchRepository {

    @ConfigProperty(name = "branch.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    private DynamoDbTable<Branch> getBranchTable() {
        return clientFactory.getEnhancedDynamoClient().table(tableName, Schemas.BRANCH_SCHEMA);
    }

    public Branch getById(String companyId, String branchId) {
        return DynamoDBUtils.getItem(getBranchTable(), companyId, branchId);
    }

    public PaginatedResponse<Branch> getByCompanyId(String companyId, String nextToken, Integer limit) {
        return DynamoDBUtils.queryByPartitionKey(getBranchTable(), companyId, nextToken, limit);
    }

    public void save(Branch branch) {
        DynamoDBUtils.putItem(getBranchTable(), branch);
    }

    public void delete(String companyId, String branchId) {
        DynamoDBUtils.deleteItem(getBranchTable(), companyId, branchId);
    }

    public String getTableName() {
        return tableName;
    }
}
