package com.fulfai.sellingpartner.companyJoinRequest;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@DynamoDbBean
@RegisterForReflection
public class CompanyJoinRequest {

    /* =========================
       PRIMARY KEYS
    ========================== */

    private String companyId;     // PK
    private String requestId;     // SK

    /* =========================
       CORE DATA
    ========================== */

    private String userId;
    private String status;        // PENDING | APPROVED | REJECTED
    private String message;

    /* =========================
       GSI ATTRIBUTES
    ========================== */

    // company-status-index
    private String companyStatus; // status#timestamp

    // user-company-index
    private String userCompany;   // companyId

    /* =========================
       AUDIT
    ========================== */

    private Instant requestedAt;
    private Instant reviewedAt;
    private String reviewedBy;

    private Instant createdAt;
    private Instant updatedAt;

    /* =========================
       PRIMARY KEY MAPPING
    ========================== */

    @DynamoDbPartitionKey
    @DynamoDbAttribute("companyId")
    public String getCompanyId() {
        return companyId;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("requestId")
    public String getRequestId() {
        return requestId;
    }

    /* =========================
       ATTRIBUTES
    ========================== */

    @DynamoDbAttribute("userId")
    public String getUserId() {
        return userId;
    }

    @DynamoDbAttribute("status")
    public String getStatus() {
        return status;
    }

    @DynamoDbAttribute("message")
    public String getMessage() {
        return message;
    }

    @DynamoDbAttribute("companyStatus")
    public String getCompanyStatus() {
        return companyStatus;
    }

    @DynamoDbAttribute("userCompany")
    public String getUserCompany() {
        return userCompany;
    }

    @DynamoDbAttribute("requestedAt")
    public Instant getRequestedAt() {
        return requestedAt;
    }

    @DynamoDbAttribute("reviewedAt")
    public Instant getReviewedAt() {
        return reviewedAt;
    }

    @DynamoDbAttribute("reviewedBy")
    public String getReviewedBy() {
        return reviewedBy;
    }

    @DynamoDbAttribute("createdAt")
    public Instant getCreatedAt() {
        return createdAt;
    }

    @DynamoDbAttribute("updatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /* =========================
       GSI: company-status-index
    ========================== */

    @DynamoDbSecondaryPartitionKey(indexNames = "company-status-index")
    public String getCompanyIdForStatusIndex() {
        return companyId;
    }

    @DynamoDbSecondarySortKey(indexNames = "company-status-index")
    public String getCompanyStatusForIndex() {
        return companyStatus;
    }

    /* =========================
       GSI: user-company-index
    ========================== */

    @DynamoDbSecondaryPartitionKey(indexNames = "user-company-index")
    public String getUserIdForCompanyIndex() {
        return userId;
    }

    @DynamoDbSecondarySortKey(indexNames = "user-company-index")
    public String getUserCompanyForIndex() {
        return userCompany;
    }
}
