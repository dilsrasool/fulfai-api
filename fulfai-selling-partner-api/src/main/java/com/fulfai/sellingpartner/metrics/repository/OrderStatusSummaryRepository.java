package com.fulfai.sellingpartner.metrics.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import com.fulfai.sellingpartner.analytics.dto.OrderStatusSummaryDTO;
import com.fulfai.sellingpartner.metrics.dto.OrderStatusSnapshot;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrderStatusSummaryRepository {

    private static final String TABLE = "order_status_summary";

    @Inject
    DynamoDbClient dynamo;

    /* =========================================================
       KEY BUILDER
    ========================================================= */
    private String key(String companyId, String branchId) {
        return companyId + "#" + branchId;
    }

    /* =========================================================
       UPSERT (branch level)
    ========================================================= */
    public void incrementStatus(
            String companyId,
            String branchId,
            String date,
            OrderStatusSnapshot delta) {

        dynamo.updateItem(UpdateItemRequest.builder()
            .tableName(TABLE)
            .key(Map.of(
                "companyBranchKey", AttributeValue.fromS(key(companyId, branchId)),
                "date", AttributeValue.fromS(date)
            ))
            .updateExpression("""
                ADD pending :p,
                    packed :pa,
                    shipped :s,
                    delivered :d,
                    cancelled :c,
                    returned :r
            """)
            .expressionAttributeValues(Map.of(
                ":p", AttributeValue.fromN(String.valueOf(delta.pending())),
                ":pa", AttributeValue.fromN(String.valueOf(delta.packed())),
                ":s", AttributeValue.fromN(String.valueOf(delta.shipped())),
                ":d", AttributeValue.fromN(String.valueOf(delta.delivered())),
                ":c", AttributeValue.fromN(String.valueOf(delta.cancelled())),
                ":r", AttributeValue.fromN(String.valueOf(delta.returned()))
            ))
            .build());
    }

    /* =========================================================
       BRANCH → single day
    ========================================================= */
    public Optional<OrderStatusSummaryDTO> getBranchDay(
            String companyId,
            String branchId,
            String date) {

        Map<String, AttributeValue> keyMap = Map.of(
            "companyBranchKey", AttributeValue.fromS(key(companyId, branchId)),
            "date", AttributeValue.fromS(date)
        );

        var item = dynamo.getItem(GetItemRequest.builder()
                .tableName(TABLE)
                .key(keyMap)
                .build())
                .item();

        if (item == null || item.isEmpty()) return Optional.empty();

        return Optional.of(map(item));
    }

    /* =========================================================
       BRANCH → trend
    ========================================================= */
    public List<OrderStatusSummaryDTO> getBranchTrend(
            String companyId,
            String branchId,
            String fromDate,
            String toDate) {

        QueryRequest request = QueryRequest.builder()
            .tableName(TABLE)
            .keyConditionExpression("companyBranchKey = :k AND #d BETWEEN :f AND :t")
            .expressionAttributeNames(Map.of("#d", "date"))
            .expressionAttributeValues(Map.of(
                ":k", AttributeValue.fromS(key(companyId, branchId)),
                ":f", AttributeValue.fromS(fromDate),
                ":t", AttributeValue.fromS(toDate)
            ))
            .build();

        return dynamo.query(request)
            .items()
            .stream()
            .map(this::map)
            .collect(Collectors.toList());
    }

    /* =========================================================
       COMPANY → aggregate TODAY
    ========================================================= */
    public OrderStatusSummaryDTO getCompanyTodaySummary(
            String companyId,
            List<String> branchIds) {

        String today = java.time.LocalDate.now().toString();

        int pending = 0, packed = 0, shipped = 0, delivered = 0, cancelled = 0, returned = 0;

        for (String branchId : branchIds) {
            var day = getBranchDay(companyId, branchId, today);

            if (day.isPresent()) {
                var d = day.get();
                pending += d.pending();
                packed += d.packed();
                shipped += d.shipped();
                delivered += d.delivered();
                cancelled += d.cancelled();
                returned += d.returned();
            }
        }

        return new OrderStatusSummaryDTO(
                pending, packed, shipped, delivered, cancelled, returned
        );
    }

    /* =========================================================
       HELPERS
    ========================================================= */
    private OrderStatusSummaryDTO map(Map<String, AttributeValue> item) {
        return new OrderStatusSummaryDTO(
                n(item, "pending"),
                n(item, "packed"),
                n(item, "shipped"),
                n(item, "delivered"),
                n(item, "cancelled"),
                n(item, "returned")
        );
    }

    private int n(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key)
                ? Integer.parseInt(item.get(key).n())
                : 0;
    }
}
