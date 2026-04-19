package com.fulfai.sellingpartner.branchreview;

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
public class BranchReview {

    // ==========================
    // GSI CONSTANTS
    // ==========================

    /** GSI: list all reviews for a branch, sorted by date */
    public static final String BY_BRANCH_INDEX = "byBranch-index";

    /** GSI: enforce one review per user per branch */
    public static final String BY_USER_BRANCH_INDEX = "byUserBranch-index";

    // ==========================
    // FIELDS
    // ==========================

    /** PK: companyId#branchId  (scopes the table per company+branch) */
    private String branchKey;

    /** SK: reviewId */
    private String reviewId;

    /** For BY_BRANCH_INDEX PK */
    private String branchId;

    /** For BY_BRANCH_INDEX SK and sort */
    private Instant createdAt;

    /** For BY_USER_BRANCH_INDEX PK */
    private String userId;

    /** Display name of the reviewer (denormalized) */
    private String userName;

    /** 1 – 5 */
    private Integer rating;

    /** Optional text comment */
    private String comment;

    /** Soft-delete flag */
    private Boolean isDeleted;

    private Instant updatedAt;

    // ==========================
    // KEY ACCESSORS
    // ==========================

    @DynamoDbPartitionKey
    @DynamoDbAttribute("branchKey")
    public String getBranchKey() {
        return branchKey;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("reviewId")
    public String getReviewId() {
        return reviewId;
    }

    // BY_BRANCH_INDEX
    @DynamoDbSecondaryPartitionKey(indexNames = BY_BRANCH_INDEX)
    @DynamoDbAttribute("branchId")
    public String getBranchId() {
        return branchId;
    }

    @DynamoDbSecondarySortKey(indexNames = BY_BRANCH_INDEX)
    @DynamoDbAttribute("createdAt")
    public Instant getCreatedAt() {
        return createdAt;
    }

    // BY_USER_BRANCH_INDEX
    @DynamoDbSecondaryPartitionKey(indexNames = BY_USER_BRANCH_INDEX)
    @DynamoDbAttribute("userId")
    public String getUserId() {
        return userId;
    }

    @DynamoDbAttribute("userName")
    public String getUserName() {
        return userName;
    }

    @DynamoDbAttribute("rating")
    public Integer getRating() {
        return rating;
    }

    @DynamoDbAttribute("comment")
    public String getComment() {
        return comment;
    }

    @DynamoDbAttribute("isDeleted")
    public Boolean getIsDeleted() {
        return isDeleted;
    }

    @DynamoDbAttribute("updatedAt")
    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
