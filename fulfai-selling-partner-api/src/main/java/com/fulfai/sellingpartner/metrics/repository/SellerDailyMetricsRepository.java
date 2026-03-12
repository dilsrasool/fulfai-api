package com.fulfai.sellingpartner.metrics.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import com.fulfai.sellingpartner.metrics.dto.SellerDailyMetricsDTO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class SellerDailyMetricsRepository {

    private static final String TABLE = "seller_daily_metrics";

    @Inject
    DynamoDbClient dynamo;

    /* =========================================================
       LAST 30 DAYS (dashboard trend)
    ========================================================= */
    public List<SellerDailyMetricsDTO> getLast30Days(String companyId) {

        String today = LocalDate.now().toString();
        String from = LocalDate.now().minusDays(30).toString();

        QueryRequest request = QueryRequest.builder()
            .tableName(TABLE)
            .keyConditionExpression("companyId = :cid AND #d BETWEEN :from AND :to")
            .expressionAttributeNames(Map.of("#d", "date"))
            .expressionAttributeValues(Map.of(
                ":cid", AttributeValue.fromS(companyId),
                ":from", AttributeValue.fromS(from),
                ":to", AttributeValue.fromS(today)
            ))
            .scanIndexForward(true)
            .build();

        return dynamo.query(request)
                .items()
                .stream()
                .map(this::mapDaily)   // ← NOW EXISTS
                .collect(Collectors.toList());
    }

    /* =========================================================
       UPSERT (called by aggregation job)
    ========================================================= */
    public void upsertDaily(
            String companyId,
            String date,
            int orders,
            double revenue) {

        dynamo.updateItem(UpdateItemRequest.builder()
            .tableName(TABLE)
            .key(Map.of(
                "companyId", AttributeValue.fromS(companyId),
                "date", AttributeValue.fromS(date)
            ))
            .updateExpression("""
                ADD orders :o,
                    revenue :r
            """)
            .expressionAttributeValues(Map.of(
                ":o", AttributeValue.fromN(String.valueOf(orders)),
                ":r", AttributeValue.fromN(String.valueOf(revenue))
            ))
            .build());
    }

    /* =========================================================
       MAPPER  ✅ THIS WAS MISSING
    ========================================================= */
    private SellerDailyMetricsDTO mapDaily(Map<String, AttributeValue> item) {

        return new SellerDailyMetricsDTO(
            item.get("date").s(),
            n(item, "orders"),
            d(item, "revenue")
        );
    }

    /* =========================================================
       HELPERS
    ========================================================= */

    private int n(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key)
                ? Integer.parseInt(item.get(key).n())
                : 0;
    }

    private double d(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key)
                ? Double.parseDouble(item.get(key).n())
                : 0;
    }
}
