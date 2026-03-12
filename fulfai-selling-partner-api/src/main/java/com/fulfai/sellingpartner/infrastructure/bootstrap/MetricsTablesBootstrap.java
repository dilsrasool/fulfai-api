package com.fulfai.sellingpartner.infrastructure.bootstrap;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public final class MetricsTablesBootstrap {

    private MetricsTablesBootstrap() {}

    /* =========================================================
       PUBLIC ENTRYPOINT
    ========================================================= */

    public static void createAll(DynamoDbClient dynamo) {
        createSellerDailyMetricsTable(dynamo, "seller_daily_metrics");
        createProductDailyMetricsTable(dynamo, "product_daily_metrics");
        createInventorySnapshotTable(dynamo, "inventory_snapshot");
        createOrderStatusSummaryTable(dynamo, "order_status_summary");
        createFinanceSummaryTable(dynamo, "finance_summary");
    }

    /* =========================================================
       1. SELLER DAILY METRICS
       KPI cards + sales charts
       PK: companyId
       SK: date (yyyy-mm-dd)
    ========================================================= */

    public static void createSellerDailyMetricsTable(DynamoDbClient dynamo, String tableName) {
        if (tableExists(dynamo, tableName)) return;

        dynamo.createTable(b -> b
            .tableName(tableName)
            .keySchema(
                key("companyId", KeyType.HASH),
                key("date", KeyType.RANGE))
            .attributeDefinitions(
                attr("companyId", ScalarAttributeType.S),
                attr("date", ScalarAttributeType.S))
            .billingMode(BillingMode.PAY_PER_REQUEST)
        );
    }

    /* =========================================================
       2. PRODUCT DAILY METRICS
       Top products / low performers
       PK: companyProductKey (company#product)
       SK: date

       GSI:
       companyDateKey (company#date) -> revenue (desc)
    ========================================================= */

    public static void createProductDailyMetricsTable(DynamoDbClient dynamo, String tableName) {
        if (tableExists(dynamo, tableName)) return;

        dynamo.createTable(b -> b
            .tableName(tableName)
            .keySchema(
                key("companyProductKey", KeyType.HASH),
                key("date", KeyType.RANGE))
            .attributeDefinitions(
                attr("companyProductKey", ScalarAttributeType.S),
                attr("date", ScalarAttributeType.S),
                attr("companyDateKey", ScalarAttributeType.S),
                attr("revenue", ScalarAttributeType.N))
            .globalSecondaryIndexes(
                GlobalSecondaryIndex.builder()
                    .indexName("TopProductsByRevenueGSI")
                    .keySchema(
                        key("companyDateKey", KeyType.HASH),
                        key("revenue", KeyType.RANGE))
                    .projection(p -> p.projectionType(ProjectionType.ALL))
                    .build())
            .billingMode(BillingMode.PAY_PER_REQUEST)
        );
    }

    /* =========================================================
       3. INVENTORY SNAPSHOT
       Current stock levels
       PK: companyId
       SK: productId
    ========================================================= */

   public static void createInventorySnapshotTable(DynamoDbClient dynamo, String tableName) {
    if (tableExists(dynamo, tableName)) return;

    dynamo.createTable(b -> b
        .tableName(tableName)
        .keySchema(
            key("companyBranchKey", KeyType.HASH),
            key("productId", KeyType.RANGE))
        .attributeDefinitions(
            attr("companyBranchKey", ScalarAttributeType.S),
            attr("productId", ScalarAttributeType.S))
        .billingMode(BillingMode.PAY_PER_REQUEST)
    );
}


    /* =========================================================
       4. ORDER STATUS SUMMARY
       Pending / shipped / delivered counts
       PK: companyId
       SK: date
    ========================================================= */

 public static void createOrderStatusSummaryTable(DynamoDbClient dynamo, String tableName) {
    if (tableExists(dynamo, tableName)) return;

    dynamo.createTable(b -> b
        .tableName(tableName)
        .keySchema(
            key("companyBranchKey", KeyType.HASH),
            key("date", KeyType.RANGE))
        .attributeDefinitions(
            attr("companyBranchKey", ScalarAttributeType.S),
            attr("date", ScalarAttributeType.S))
        .billingMode(BillingMode.PAY_PER_REQUEST)
    );
}


    /* =========================================================
       5. FINANCE SUMMARY
       Payouts / commissions
       PK: companyId
       SK: period (yyyy-mm)
    ========================================================= */

    public static void createFinanceSummaryTable(DynamoDbClient dynamo, String tableName) {
        if (tableExists(dynamo, tableName)) return;

        dynamo.createTable(b -> b
            .tableName(tableName)
            .keySchema(
                key("companyId", KeyType.HASH),
                key("period", KeyType.RANGE))
            .attributeDefinitions(
                attr("companyId", ScalarAttributeType.S),
                attr("period", ScalarAttributeType.S))
            .billingMode(BillingMode.PAY_PER_REQUEST)
        );
    }

    /* =========================================================
       COMMON HELPERS
    ========================================================= */

    private static KeySchemaElement key(String name, KeyType type) {
        return KeySchemaElement.builder()
            .attributeName(name)
            .keyType(type)
            .build();
    }

    private static AttributeDefinition attr(String name, ScalarAttributeType type) {
        return AttributeDefinition.builder()
            .attributeName(name)
            .attributeType(type)
            .build();
    }

    private static boolean tableExists(DynamoDbClient dynamo, String tableName) {
        try {
            dynamo.describeTable(r -> r.tableName(tableName));
            return true;
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
}
