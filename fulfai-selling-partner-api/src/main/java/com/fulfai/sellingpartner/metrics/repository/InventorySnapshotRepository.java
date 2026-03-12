package com.fulfai.sellingpartner.metrics.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import com.fulfai.sellingpartner.metrics.dto.*;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class InventorySnapshotRepository {

    private static final String TABLE = "inventory_snapshot";

    @Inject
    DynamoDbClient dynamo;

    /* =========================================================
       KEY BUILDER
    ========================================================= */
    private String branchKey(String companyId, String branchId) {
        return companyId + "#" + branchId;
    }

    /* =========================================================
       UPSERT
       Replace entire snapshot for a product
    ========================================================= */
    public void upsertSnapshot(
            String companyId,
            String branchId,
            String productId,
            int stock,
            int reserved,
            int reorderLevel,
            int daysOfInventory) {

        dynamo.putItem(PutItemRequest.builder()
            .tableName(TABLE)
            .item(Map.of(
                "companyBranchKey", AttributeValue.fromS(branchKey(companyId, branchId)),
                "productId", AttributeValue.fromS(productId),
                "stock", AttributeValue.fromN(String.valueOf(stock)),
                "reserved", AttributeValue.fromN(String.valueOf(reserved)),
                "reorderLevel", AttributeValue.fromN(String.valueOf(reorderLevel)),
                "daysOfInventory", AttributeValue.fromN(String.valueOf(daysOfInventory))
            ))
            .build());
    }

    /* =========================================================
       GET ALL INVENTORY (branch only)
    ========================================================= */
    public List<InventorySnapshotDTO> getAll(
            String companyId,
            String branchId) {

        QueryRequest request = QueryRequest.builder()
            .tableName(TABLE)
            .keyConditionExpression("companyBranchKey = :k")
            .expressionAttributeValues(Map.of(
                ":k", AttributeValue.fromS(branchKey(companyId, branchId))
            ))
            .build();

        return dynamo.query(request)
            .items()
            .stream()
            .map(this::mapSnapshot)
            .collect(Collectors.toList());
    }

    /* =========================================================
       LOW STOCK ALERTS
    ========================================================= */
  public List<LowStockDTO> getLowStock(
        String companyId,
        String branchId) {

    return getAll(companyId, branchId)
        .stream()
        .filter(i -> i.stock() <= i.reorderLevel())
        .map(i -> new LowStockDTO(
                i.branchId(),
                i.productId(),
                i.stock(),
                i.reorderLevel()
        ))
        .toList();
}


    /* =========================================================
       GET SINGLE PRODUCT
    ========================================================= */
    public Optional<InventorySnapshotDTO> get(
            String companyId,
            String branchId,
            String productId) {

        var item = dynamo.getItem(r -> r
            .tableName(TABLE)
            .key(Map.of(
                "companyBranchKey", AttributeValue.fromS(branchKey(companyId, branchId)),
                "productId", AttributeValue.fromS(productId)
            ))).item();

        if (item == null || item.isEmpty()) return Optional.empty();

        return Optional.of(mapSnapshot(item));
    }

    /* =========================================================
       BULK UPSERT
    ========================================================= */
    public void batchUpsert(
            String companyId,
            String branchId,
            List<InventorySnapshotDTO> snapshots) {

        List<WriteRequest> writes = new ArrayList<>();

        for (InventorySnapshotDTO s : snapshots) {

            writes.add(WriteRequest.builder()
                .putRequest(PutRequest.builder()
                    .item(Map.of(
                        "companyBranchKey", AttributeValue.fromS(branchKey(companyId, branchId)),
                        "productId", AttributeValue.fromS(s.productId()),
                        "stock", AttributeValue.fromN(String.valueOf(s.stock())),
                        "reserved", AttributeValue.fromN(String.valueOf(s.reserved())),
                        "reorderLevel", AttributeValue.fromN(String.valueOf(s.reorderLevel())),
                        "daysOfInventory", AttributeValue.fromN(String.valueOf(s.daysOfInventory()))
                    ))
                    .build())
                .build());
        }

        dynamo.batchWriteItem(b -> b.requestItems(Map.of(TABLE, writes)));
    }

    /* =========================================================
       HELPERS
    ========================================================= */

    private InventorySnapshotDTO mapSnapshot(Map<String, AttributeValue> item) {

        // companyBranchKey = company#branch
        String key = item.get("companyBranchKey").s();
        String branchId = key.substring(key.indexOf("#") + 1);

        return new InventorySnapshotDTO(
            branchId,
            item.get("productId").s(),
            n(item, "stock"),
            n(item, "reserved"),
            n(item, "reorderLevel"),
            n(item, "daysOfInventory")
        );
    }

    private int n(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key)
                ? Integer.parseInt(item.get(key).n())
                : 0;
    }

    public List<LowStockDTO> getLowStockAllBranches(String companyId, List<String> branchIds) {
    return branchIds.stream()
            .flatMap(b -> getLowStock(companyId, b).stream())
            .toList();
}

}
