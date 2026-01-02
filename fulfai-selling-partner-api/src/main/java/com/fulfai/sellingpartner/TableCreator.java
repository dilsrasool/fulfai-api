package com.fulfai.sellingpartner;


import com.fulfai.sellingpartner.category.Category;
import com.fulfai.sellingpartner.order.Order;
import com.fulfai.sellingpartner.product.Product;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

public class TableCreator {

    /* =========================
       COMMON
    ========================== */

    protected static boolean tableExists(DynamoDbClient dynamoDbClient, String tableName) {
        ListTablesResponse tables = dynamoDbClient.listTables();
        return tables.tableNames().contains(tableName);
    }

    /* =========================
       COMPANY
    ========================== */

    public static void createCompanyTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) return;

        dynamoDbClient.createTable(builder -> builder
            .tableName(tableName)
            .keySchema(KeySchemaElement.builder()
                .attributeName("id")
                .keyType(KeyType.HASH)
                .build())
            .attributeDefinitions(
                AttributeDefinition.builder()
                    .attributeName("id")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("ownerSub")
                    .attributeType(ScalarAttributeType.S)
                    .build())
            .globalSecondaryIndexes(
                GlobalSecondaryIndex.builder()
                    .indexName("ownerSub-index")
                    .keySchema(KeySchemaElement.builder()
                        .attributeName("ownerSub")
                        .keyType(KeyType.HASH)
                        .build())
                    .projection(Projection.builder()
                        .projectionType(ProjectionType.ALL)
                        .build())
                    .build())
            .billingMode(BillingMode.PAY_PER_REQUEST)
        );
    }

    /* =========================
       BRANCH
    ========================== */

    public static void createBranchTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) return;

        dynamoDbClient.createTable(builder -> builder
            .tableName(tableName)
            .keySchema(
                KeySchemaElement.builder()
                    .attributeName("companyId")
                    .keyType(KeyType.HASH)
                    .build(),
                KeySchemaElement.builder()
                    .attributeName("branchId")
                    .keyType(KeyType.RANGE)
                    .build())
            .attributeDefinitions(
                AttributeDefinition.builder()
                    .attributeName("companyId")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("branchId")
                    .attributeType(ScalarAttributeType.S)
                    .build())
            .billingMode(BillingMode.PAY_PER_REQUEST)
        );
    }

    /* =========================
       CATEGORY
    ========================== */

    public static void createCategoryTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) return;

        dynamoDbClient.createTable(builder -> builder
            .tableName(tableName)
            .keySchema(KeySchemaElement.builder()
                .attributeName("name")
                .keyType(KeyType.HASH)
                .build())
            .attributeDefinitions(
                AttributeDefinition.builder()
                    .attributeName("name")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("parentCategory")
                    .attributeType(ScalarAttributeType.S)
                    .build())
            .globalSecondaryIndexes(
                GlobalSecondaryIndex.builder()
                    .indexName(Category.PARENT_GSI)
                    .keySchema(KeySchemaElement.builder()
                        .attributeName("parentCategory")
                        .keyType(KeyType.HASH)
                        .build())
                    .projection(Projection.builder()
                        .projectionType(ProjectionType.ALL)
                        .build())
                    .build())
            .billingMode(BillingMode.PAY_PER_REQUEST)
        );
    }

    /* =========================
       PRODUCT
    ========================== */

    public static void createProductTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) return;

        dynamoDbClient.createTable(builder -> builder
            .tableName(tableName)
            .keySchema(
                KeySchemaElement.builder()
                    .attributeName("companyId")
                    .keyType(KeyType.HASH)
                    .build(),
                KeySchemaElement.builder()
                    .attributeName("branchProductKey")
                    .keyType(KeyType.RANGE)
                    .build())
            .attributeDefinitions(
                AttributeDefinition.builder()
                    .attributeName("companyId")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("branchProductKey")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("category")
                    .attributeType(ScalarAttributeType.S)
                    .build())
            .globalSecondaryIndexes(
                GlobalSecondaryIndex.builder()
                    .indexName(Product.CATEGORY_GSI)
                    .keySchema(
                        KeySchemaElement.builder()
                            .attributeName("category")
                            .keyType(KeyType.HASH)
                            .build(),
                        KeySchemaElement.builder()
                            .attributeName("companyId")
                            .keyType(KeyType.RANGE)
                            .build())
                    .projection(Projection.builder()
                        .projectionType(ProjectionType.ALL)
                        .build())
                    .build())
            .billingMode(BillingMode.PAY_PER_REQUEST)
        );
    }

    /* =========================
       ORDER
    ========================== */

    public static void createOrderTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) return;

        dynamoDbClient.createTable(builder -> builder
            .tableName(tableName)
            .keySchema(
                KeySchemaElement.builder()
                    .attributeName("companyId")
                    .keyType(KeyType.HASH)
                    .build(),
                KeySchemaElement.builder()
                    .attributeName("orderId")
                    .keyType(KeyType.RANGE)
                    .build())
            .attributeDefinitions(
                AttributeDefinition.builder()
                    .attributeName("companyId")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("orderId")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("orderDate")
                    .attributeType(ScalarAttributeType.S)
                    .build())
            .globalSecondaryIndexes(
                GlobalSecondaryIndex.builder()
                    .indexName(Order.DATE_GSI)
                    .keySchema(
                        KeySchemaElement.builder()
                            .attributeName("companyId")
                            .keyType(KeyType.HASH)
                            .build(),
                        KeySchemaElement.builder()
                            .attributeName("orderDate")
                            .keyType(KeyType.RANGE)
                            .build())
                    .projection(Projection.builder()
                        .projectionType(ProjectionType.ALL)
                        .build())
                    .build())
            .billingMode(BillingMode.PAY_PER_REQUEST)
        );
    }

    /* =========================
       ACCOUNT
    ========================== */

    public static void createAccountTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) return;

        dynamoDbClient.createTable(builder -> builder
            .tableName(tableName)
            .keySchema(
                KeySchemaElement.builder()
                    .attributeName("companyAccountKey")
                    .keyType(KeyType.HASH)
                    .build(),
                KeySchemaElement.builder()
                    .attributeName("date")
                    .keyType(KeyType.RANGE)
                    .build())
            .attributeDefinitions(
                AttributeDefinition.builder()
                    .attributeName("companyAccountKey")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("date")
                    .attributeType(ScalarAttributeType.S)
                    .build())
            .billingMode(BillingMode.PAY_PER_REQUEST)
        );
    }

    /* =========================
       USER COMPANY ROLE
    ========================== */

    public static void createUserCompanyRoleTable(
            DynamoDbClient dynamoDbClient,
            String tableName
    ) {
        if (tableExists(dynamoDbClient, tableName)) return;

        dynamoDbClient.createTable(builder -> builder
            .tableName(tableName)
            .keySchema(
                KeySchemaElement.builder()
                    .attributeName("userId")
                    .keyType(KeyType.HASH)
                    .build(),
                KeySchemaElement.builder()
                    .attributeName("companyBranch")
                    .keyType(KeyType.RANGE)
                    .build())
            .attributeDefinitions(
                AttributeDefinition.builder()
                    .attributeName("userId")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("companyBranch")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("companyId")
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName("branchUser")
                    .attributeType(ScalarAttributeType.S)
                    .build())
            .globalSecondaryIndexes(
                GlobalSecondaryIndex.builder()
                    .indexName("companyId-index")
                    .keySchema(
                        KeySchemaElement.builder()
                            .attributeName("companyId")
                            .keyType(KeyType.HASH)
                            .build(),
                        KeySchemaElement.builder()
                            .attributeName("branchUser")
                            .keyType(KeyType.RANGE)
                            .build())
                    .projection(Projection.builder()
                        .projectionType(ProjectionType.ALL)
                        .build())
                    .build())
            .billingMode(BillingMode.PAY_PER_REQUEST)
        );
    }


    /* =========================
   COMPANY JOIN REQUEST
========================== */

public static void createCompanyJoinRequestTable(
        DynamoDbClient dynamoDbClient,
        String tableName
) {
    if (tableExists(dynamoDbClient, tableName)) return;

    dynamoDbClient.createTable(builder -> builder
        .tableName(tableName)

        /* =========================
           PRIMARY KEY
           PK: companyId
           SK: requestId
        ========================== */
        .keySchema(
            KeySchemaElement.builder()
                .attributeName("companyId")
                .keyType(KeyType.HASH)
                .build(),
            KeySchemaElement.builder()
                .attributeName("requestId")
                .keyType(KeyType.RANGE)
                .build()
        )

        /* =========================
           ATTRIBUTES
        ========================== */
        .attributeDefinitions(
            AttributeDefinition.builder()
                .attributeName("companyId")
                .attributeType(ScalarAttributeType.S)
                .build(),
            AttributeDefinition.builder()
                .attributeName("requestId")
                .attributeType(ScalarAttributeType.S)
                .build(),
            AttributeDefinition.builder()
                .attributeName("userId")
                .attributeType(ScalarAttributeType.S)
                .build(),
            AttributeDefinition.builder()
                .attributeName("companyStatus")
                .attributeType(ScalarAttributeType.S)
                .build(),
            AttributeDefinition.builder()
                .attributeName("userCompany")
                .attributeType(ScalarAttributeType.S)
                .build()
        )

        /* =========================
           GSI: company-status-index
           → Admin view (pending requests per company)
        ========================== */
        .globalSecondaryIndexes(
            GlobalSecondaryIndex.builder()
                .indexName("company-status-index")
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("companyId")
                        .keyType(KeyType.HASH)
                        .build(),
                    KeySchemaElement.builder()
                        .attributeName("companyStatus")
                        .keyType(KeyType.RANGE)
                        .build()
                )
                .projection(Projection.builder()
                    .projectionType(ProjectionType.ALL)
                    .build()
                )
                .build(),

        /* =========================
           GSI: user-company-index
           → Prevent duplicate requests
        ========================== */
            GlobalSecondaryIndex.builder()
                .indexName("user-company-index")
                .keySchema(
                    KeySchemaElement.builder()
                        .attributeName("userId")
                        .keyType(KeyType.HASH)
                        .build(),
                    KeySchemaElement.builder()
                        .attributeName("userCompany")
                        .keyType(KeyType.RANGE)
                        .build()
                )
                .projection(Projection.builder()
                    .projectionType(ProjectionType.ALL)
                    .build()
                )
                .build()
        )

        .billingMode(BillingMode.PAY_PER_REQUEST)
    );
}


   
}
