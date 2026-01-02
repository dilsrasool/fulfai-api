package com.fulfai.sellingpartner.companyJoinRequest;

import java.time.Instant;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

@Data
@DynamoDbBean
@RegisterForReflection
public class CompanyJoinRequest {

    /* =========================
       PRIMARY KEYS
    ========================== */

    private String companyId;   // PK
    private String requestId;   // SK (UUID)

    /* =========================
       CORE DATA
    ========================== */

    private String userId;
    private String status;      // PENDING | APPROVED | REJECTED
    private String joinCode;
    private String message;

    private Instant createdAt;
    private Instant updatedAt;


    /* =========================
       AUDIT
    ========================== */

    private Instant requestedAt;
    private Instant reviewedAt;
    private String reviewedBy;

    /* =========================
       GSIs
    ========================== */

    // GSI 1: company + status
    private String gsi1Pk;      // companyId
    private String gsi1Sk;      // status#requestedAt

    // GSI 2: user + company (prevent duplicates)
    private String gsi2Pk;      // userId
    private String gsi2Sk;      // companyId

    /* =========================
       KEY ANNOTATIONS
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

    @DynamoDbAttribute("joinCode")
    public String getJoinCode() {
        return joinCode;
    }

    @DynamoDbAttribute("message")
    public String getMessage() {
        return message;
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
       GSI 1: company-status-index
    ========================== */

    @DynamoDbSecondaryPartitionKey(indexNames = "company-status-index")
    @DynamoDbAttribute("GSI1PK")
    public String getGsi1Pk() {
        return gsi1Pk;
    }

    @DynamoDbSecondarySortKey(indexNames = "company-status-index")
    @DynamoDbAttribute("GSI1SK")
    public String getGsi1Sk() {
        return gsi1Sk;
    }

    /* =========================
       GSI 2: user-company-index
    ========================== */

    @DynamoDbSecondaryPartitionKey(indexNames = "user-company-index")
    @DynamoDbAttribute("GSI2PK")
    public String getGsi2Pk() {
        return gsi2Pk;
    }

    @DynamoDbSecondarySortKey(indexNames = "user-company-index")
    @DynamoDbAttribute("GSI2SK")
    public String getGsi2Sk() {
        return gsi2Sk;
    }
}
