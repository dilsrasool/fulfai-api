package com.fulfai.sellingpartner.metrics.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import com.fulfai.sellingpartner.metrics.dto.*;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductDailyMetricsRepository {

    private static final String TABLE = "product_daily_metrics";
    private static final String TOP_PRODUCTS_GSI = "TopProductsByRevenueGSI";

    @Inject
    DynamoDbClient dynamo;

    /* =========================================================
       UPSERT (aggregation job)
    ========================================================= */
    public void upsertProductDailyMetrics(
            String companyId,
            String date,
            ProductMetricsSnapshot snapshot) {

        String companyProductKey = companyId + "#" + snapshot.productId();
        String companyDateKey = companyId + "#" + date;

        dynamo.updateItem(UpdateItemRequest.builder()
            .tableName(TABLE)
            .key(Map.of(
                "companyProductKey", AttributeValue.fromS(companyProductKey),
                "date", AttributeValue.fromS(date)
            ))
            .updateExpression("""
                ADD unitsSold :u,
                    revenue :r
                SET stock = :s,
                    companyDateKey = :cd
            """)
            .expressionAttributeValues(Map.of(
                ":u", AttributeValue.fromN(String.valueOf(snapshot.units())),
                ":r", AttributeValue.fromN(String.valueOf(snapshot.revenue())),
                ":s", AttributeValue.fromN(String.valueOf(snapshot.stock())),
                ":cd", AttributeValue.fromS(companyDateKey)
            ))
            .build());
    }

    /* =========================================================
       TOP PRODUCTS (by revenue)
       Uses GSI → SUPER FAST
    ========================================================= */
    public List<TopProductDTO> getTopProducts(
            String companyId,
            String date,
            int limit) {

        String companyDateKey = companyId + "#" + date;

        QueryRequest request = QueryRequest.builder()
            .tableName(TABLE)
            .indexName(TOP_PRODUCTS_GSI)
            .keyConditionExpression("companyDateKey = :k")
            .expressionAttributeValues(Map.of(
                ":k", AttributeValue.fromS(companyDateKey)
            ))
            .scanIndexForward(false) // DESC (highest revenue first)
            .limit(limit)
            .build();

        return dynamo.query(request)
            .items()
            .stream()
            .map(i -> new TopProductDTO(
                    extractProductId(i),
                    n(i, "unitsSold"),
                    d(i, "revenue")
            ))
            .collect(Collectors.toList());
    }

    /* =========================================================
       PRODUCT TREND (daily chart)
    ========================================================= */
    public List<ProductDailySalesDTO> getProductTrend(
            String companyId,
            String productId,
            String fromDate,
            String toDate) {

        String companyProductKey = companyId + "#" + productId;

        QueryRequest request = QueryRequest.builder()
            .tableName(TABLE)
            .keyConditionExpression("companyProductKey = :k AND #d BETWEEN :f AND :t")
            .expressionAttributeNames(Map.of("#d", "date"))
            .expressionAttributeValues(Map.of(
                ":k", AttributeValue.fromS(companyProductKey),
                ":f", AttributeValue.fromS(fromDate),
                ":t", AttributeValue.fromS(toDate)
            ))
            .build();

        return dynamo.query(request)
            .items()
            .stream()
            .map(i -> new ProductDailySalesDTO(
                    s(i, "date"),
                    n(i, "unitsSold"),
                    d(i, "revenue")
            ))
            .collect(Collectors.toList());
    }

    /* =========================================================
       HELPERS
    ========================================================= */

    private String extractProductId(Map<String, AttributeValue> item) {
        String key = item.get("companyProductKey").s();
        return key.substring(key.indexOf("#") + 1);
    }

    private int n(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) ? Integer.parseInt(item.get(key).n()) : 0;
    }

    private double d(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) ? Double.parseDouble(item.get(key).n()) : 0;
    }

    private String s(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) ? item.get(key).s() : "";
    }
}
