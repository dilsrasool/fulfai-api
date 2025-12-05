package com.fulfai.partner;

import java.util.Arrays;

import com.fulfai.partner.order.Order;
import com.fulfai.partner.product.Product;

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

    protected static boolean tableExists(DynamoDbClient dynamoDbClient, String tableName) {
        ListTablesResponse tables = dynamoDbClient.listTables();
        return tables.tableNames().contains(tableName);
    }

    public static void createCompanyTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) {
            return;
        }

        dynamoDbClient.createTable(builder -> builder
                .tableName(tableName)
                .keySchema(KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .billingMode(BillingMode.PAY_PER_REQUEST));
    }

    public static void createBranchTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) {
            return;
        }

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
                .billingMode(BillingMode.PAY_PER_REQUEST));
    }

    public static void createCategoryTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) {
            return;
        }

        dynamoDbClient.createTable(builder -> builder
                .tableName(tableName)
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("companyId")
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("name")
                                .keyType(KeyType.RANGE)
                                .build())
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("companyId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("name")
                                .attributeType(ScalarAttributeType.S)
                                .build())
                .billingMode(BillingMode.PAY_PER_REQUEST));
    }

    public static void createProductTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) {
            return;
        }

        dynamoDbClient.createTable(builder -> builder
                .tableName(tableName)
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("companyId")
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("productId")
                                .keyType(KeyType.RANGE)
                                .build())
                .attributeDefinitions(Arrays.asList(
                        AttributeDefinition.builder()
                                .attributeName("companyId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("productId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("category")
                                .attributeType(ScalarAttributeType.S)
                                .build()))
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName(Product.CATEGORY_GSI)
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName("companyId")
                                                .keyType(KeyType.HASH)
                                                .build(),
                                        KeySchemaElement.builder()
                                                .attributeName("category")
                                                .keyType(KeyType.RANGE)
                                                .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .build())
                .billingMode(BillingMode.PAY_PER_REQUEST));
    }

    public static void createOrderTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) {
            return;
        }

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
                .attributeDefinitions(Arrays.asList(
                        AttributeDefinition.builder()
                                .attributeName("companyId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("orderId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("dateStatusKey")
                                .attributeType(ScalarAttributeType.S)
                                .build()))
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName(Order.DATE_STATUS_GSI)
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName("companyId")
                                                .keyType(KeyType.HASH)
                                                .build(),
                                        KeySchemaElement.builder()
                                                .attributeName("dateStatusKey")
                                                .keyType(KeyType.RANGE)
                                                .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .build())
                .billingMode(BillingMode.PAY_PER_REQUEST));
    }
}
