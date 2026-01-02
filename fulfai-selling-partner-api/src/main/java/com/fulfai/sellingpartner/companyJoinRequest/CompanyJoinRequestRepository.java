package com.fulfai.sellingpartner.companyJoinRequest;

import java.time.Instant;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.dynamodb.ClientFactory;
import com.fulfai.common.dynamodb.DynamoDBUtils;
import com.fulfai.common.dto.PaginatedResponse;
import com.fulfai.sellingpartner.Schemas;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactUpdateItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@ApplicationScoped
@RegisterForReflection
public class CompanyJoinRequestRepository {

    private static final String STATUS_PENDING  = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";

    @ConfigProperty(name = "companyJoinRequest.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    /* =========================
       TABLE (PRIVATE)
    ========================== */

    private DynamoDbTable<CompanyJoinRequest> table() {
        return clientFactory
                .getEnhancedDynamoClient()
                .table(tableName, Schemas.COMPANY_JOIN_REQUEST_SCHEMA);
    }

    /* =========================
       CREATE / UPDATE
    ========================== */

    public void save(CompanyJoinRequest request) {
        DynamoDBUtils.putItem(table(), request);
    }

    /* =========================
       GET BY COMPANY + REQUEST ID
    ========================== */

    public CompanyJoinRequest getByCompanyAndRequestId(
            String companyId,
            String requestId
    ) {
        return DynamoDBUtils.getItem(
                table(),
                companyId,
                requestId
        );
    }

    /* =========================
       LIST REQUESTS BY STATUS
    ========================== */

    public PaginatedResponse<CompanyJoinRequest> listByCompanyAndStatus(
            String companyId,
            String status,
            String nextToken,
            Integer limit
    ) {

        if (status == null || status.isBlank()) {
            return DynamoDBUtils.queryByPartitionKey(
                    table(),
                    companyId,
                    nextToken,
                    limit
            );
        }

        DynamoDbIndex<CompanyJoinRequest> index =
                table().index("company-status-index");

        return DynamoDBUtils.queryGsiByPartitionKeyAndSortKeyBeginsWith(
                index,
                companyId,
                status + "#",
                nextToken,
                limit
        );
    }

    /* =========================
       CHECK DUPLICATE REQUEST
    ========================== */

    public boolean existsPendingRequest(
            String userId,
            String companyId
    ) {

        DynamoDbIndex<CompanyJoinRequest> index =
                table().index("user-company-index");

        String sortKeyPrefix = companyId + "#" + STATUS_PENDING;

        PaginatedResponse<CompanyJoinRequest> response =
                DynamoDBUtils.queryGsiByPartitionKeyAndSortKeyBeginsWith(
                        index,
                        userId,
                        sortKeyPrefix,
                        null,
                        1
                );

        return response.getItems() != null &&
               !response.getItems().isEmpty();
    }

    /* =========================
       APPROVE (TRANSACTIONAL UPDATE ONLY)
    ========================== */

    public void approveJoinRequestTransactional(
            CompanyJoinRequest request,
            String approvedByUserId
    ) {

        Instant now = Instant.now();

        request.setStatus(STATUS_APPROVED);
        request.setReviewedBy(approvedByUserId);
        request.setReviewedAt(now);

        DynamoDBUtils.transactWriteItems(
                clientFactory.getEnhancedDynamoClient(),
                tx -> tx.addUpdateItem(
                        table(),
                        TransactUpdateItemEnhancedRequest.builder(CompanyJoinRequest.class)
                                .item(request)
                                .conditionExpression(
                                        Expression.builder()
                                                .expression("status = :expected")
                                                .expressionValues(
                                                        Map.of(
                                                                ":expected",
                                                                AttributeValue.fromS(STATUS_PENDING)
                                                        )
                                                )
                                                .build()
                                )
                                .build()
                )
        );
    }

    /* =========================
       DELETE
    ========================== */

    public void delete(CompanyJoinRequest request) {
        DynamoDBUtils.deleteItem(
                table(),
                request.getCompanyId(),
                request.getRequestId()
        );
    }
}
