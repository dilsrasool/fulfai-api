package com.fulfai.deliverypartner;

import java.util.Arrays;

import com.fulfai.deliverypartner.assignment.DriverOrderAssignment;
import com.fulfai.deliverypartner.driver.Driver;
import com.fulfai.deliverypartner.location.DriverLocation;

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

    public static void createDriverTable(DynamoDbClient dynamoDbClient, String tableName) {
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
                                .attributeName("driverId")
                                .keyType(KeyType.RANGE)
                                .build())
                .attributeDefinitions(Arrays.asList(
                        AttributeDefinition.builder()
                                .attributeName("companyId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("driverId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("status")
                                .attributeType(ScalarAttributeType.S)
                                .build()))
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName(Driver.STATUS_GSI)
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName("status")
                                                .keyType(KeyType.HASH)
                                                .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .build())
                .billingMode(BillingMode.PAY_PER_REQUEST));
    }

    public static void createAssignmentTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) {
            return;
        }

        dynamoDbClient.createTable(builder -> builder
                .tableName(tableName)
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("driverId")
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("assignedAt")
                                .keyType(KeyType.RANGE)
                                .build())
                .attributeDefinitions(Arrays.asList(
                        AttributeDefinition.builder()
                                .attributeName("driverId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("assignedAt")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("orderId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("status")
                                .attributeType(ScalarAttributeType.S)
                                .build()))
                .globalSecondaryIndexes(Arrays.asList(
                        GlobalSecondaryIndex.builder()
                                .indexName(DriverOrderAssignment.ORDER_GSI)
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName("orderId")
                                                .keyType(KeyType.HASH)
                                                .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .build(),
                        GlobalSecondaryIndex.builder()
                                .indexName(DriverOrderAssignment.STATUS_GSI)
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName("status")
                                                .keyType(KeyType.HASH)
                                                .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .build()))
                .billingMode(BillingMode.PAY_PER_REQUEST));
    }

    public static void createLocationTable(DynamoDbClient dynamoDbClient, String tableName) {
        if (tableExists(dynamoDbClient, tableName)) {
            return;
        }

        dynamoDbClient.createTable(builder -> builder
                .tableName(tableName)
                .keySchema(
                        KeySchemaElement.builder()
                                .attributeName("driverId")
                                .keyType(KeyType.HASH)
                                .build(),
                        KeySchemaElement.builder()
                                .attributeName("timestamp")
                                .keyType(KeyType.RANGE)
                                .build())
                .attributeDefinitions(Arrays.asList(
                        AttributeDefinition.builder()
                                .attributeName("driverId")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("timestamp")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("geohash")
                                .attributeType(ScalarAttributeType.S)
                                .build()))
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName(DriverLocation.GEOHASH_GSI)
                                .keySchema(
                                        KeySchemaElement.builder()
                                                .attributeName("geohash")
                                                .keyType(KeyType.HASH)
                                                .build(),
                                        KeySchemaElement.builder()
                                                .attributeName("timestamp")
                                                .keyType(KeyType.RANGE)
                                                .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.ALL)
                                        .build())
                                .build())
                .billingMode(BillingMode.PAY_PER_REQUEST));
    }
}
