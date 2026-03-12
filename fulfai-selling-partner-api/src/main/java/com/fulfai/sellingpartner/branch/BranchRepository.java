package com.fulfai.sellingpartner.branch;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.Schemas;

import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

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

    public List<Branch> getAllActiveBranchesAcrossAllCompanies() {
        Log.debug("*****Getting all ACTIVE branches across all companies*****\n");
    List<Branch> branchList =  getBranchTable()
            .scan(ScanEnhancedRequest.builder().build())
            .items()
            .stream()
            .filter(b -> Boolean.TRUE.equals(b.getIsActive()))
            .collect(Collectors.toList());
        Log.debugf("Found %d active branches across all companies", branchList.size());
        return branchList;
}

/* =========================================================
   GET ALL BRANCHES FOR A COMPANY (no pagination)
   Used by AnalyticsService
========================================================= */
public List<Branch> getAll(String companyId) {

    Log.debugf("Fetching all branches for companyId=%s", companyId);

    return getByCompanyId(companyId, null, null)
            .getItems();
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
