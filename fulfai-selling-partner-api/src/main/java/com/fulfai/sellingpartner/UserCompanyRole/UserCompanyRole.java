package com.fulfai.sellingpartner.UserCompanyRole;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@Data
@DynamoDbBean
@RegisterForReflection
public class UserCompanyRole {

    /* ============================
       PRIMARY KEYS
    ============================= */

    private String userId;          // PK
    private String companyBranch;   // SK → companyId#ROOT OR companyId#branchId

    /* ============================
       ATTRIBUTES
    ============================= */

    private String companyId;       // GSI PK
    private String branchId;        // nullable (ROOT = company-level)
    private String role;            // OWNER | ADMIN | MANAGER | STAFF

    /* ============================
       PRIMARY KEYS
    ============================= */

    @DynamoDbPartitionKey
    @DynamoDbAttribute("userId")
    public String getUserId() {
        return userId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("companyBranch")
    public String getCompanyBranch() {
        return companyBranch;
    }

    /* ============================
       GLOBAL SECONDARY INDEX
    ============================= */

    @DynamoDbSecondaryPartitionKey(indexNames = "companyId-index")
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbSecondarySortKey(indexNames = "companyId-index")
    @DynamoDbAttribute("branchUser")
    public String getBranchUser() {
        return (branchId == null ? "ROOT" : branchId) + "#" + userId;
    }

    /* ============================
       HELPERS
    ============================= */

    public void setCompanyAndBranch(String companyId, String branchId) {
        this.companyId = companyId;
        this.branchId = branchId;
        this.companyBranch = companyId + "#" + (branchId == null ? "ROOT" : branchId);
    }

    @DynamoDbIgnore
    public boolean isCompanyLevel() {
        return branchId == null;
    }

    @DynamoDbIgnore
    public boolean isBranchLevel() {
        return branchId != null;
    }

    @DynamoDbAttribute("role")
    public String getRole() {
        return role;
    }
}
