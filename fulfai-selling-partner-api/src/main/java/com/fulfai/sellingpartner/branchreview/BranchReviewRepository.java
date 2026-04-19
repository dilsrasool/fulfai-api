package com.fulfai.sellingpartner.branchreview;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.Schemas;

import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;

@ApplicationScoped
@RegisterForReflection
public class BranchReviewRepository {

    @ConfigProperty(name = "branchReview.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    private DynamoDbTable<BranchReview> getTable() {
        return clientFactory.getEnhancedDynamoClient().table(tableName, Schemas.BRANCH_REVIEW_SCHEMA);
    }

    public void save(BranchReview review) {
        DynamoDBUtils.putItem(getTable(), review);
    }

    public BranchReview getById(String branchKey, String reviewId) {
        return DynamoDBUtils.getItem(getTable(), branchKey, reviewId);
    }

    /**
     * List all reviews for a branch using the byBranch-index GSI.
     * Returns newest-first (descending by createdAt).
     */
    public PaginatedResponse<BranchReview> listByBranch(String branchId, String nextToken, Integer limit) {
        Log.debugf("BRANCH_REVIEW list by branchId=%s", branchId);
        DynamoDbIndex<BranchReview> index = getTable().index(BranchReview.BY_BRANCH_INDEX);
        return DynamoDBUtils.queryGsiByPartitionKey(index, branchId, nextToken, limit);
    }

    /**
     * Find an existing review by user for a specific branch (enforce 1 per user per branch).
     */
    public Optional<BranchReview> findByUserAndBranch(String userId, String branchKey) {
        Log.debugf("BRANCH_REVIEW find by userId=%s branchKey=%s", userId, branchKey);
        DynamoDbIndex<BranchReview> index = getTable().index(BranchReview.BY_USER_BRANCH_INDEX);
        PaginatedResponse<BranchReview> result =
                DynamoDBUtils.queryGsiByPartitionKeyAndSortKey(index, userId, branchKey, null, 1);
        List<BranchReview> items = result.getItems();
        return items.isEmpty() ? Optional.empty() : Optional.of(items.get(0));
    }

    public void delete(String branchKey, String reviewId) {
        DynamoDBUtils.deleteItem(getTable(), branchKey, reviewId);
    }

    public String getTableName() {
        return tableName;
    }
}
