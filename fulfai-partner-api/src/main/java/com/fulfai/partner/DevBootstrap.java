package com.fulfai.partner;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@IfBuildProfile("dev")
@Startup
@ApplicationScoped
public class DevBootstrap {

    @Inject
    DynamoDbClient dynamoDbClient;

    @ConfigProperty(name = "company.table.name")
    String companyTableName;

    @ConfigProperty(name = "branch.table.name")
    String branchTableName;

    @ConfigProperty(name = "category.table.name")
    String categoryTableName;

    @ConfigProperty(name = "product.table.name")
    String productTableName;

    @ConfigProperty(name = "order.table.name")
    String orderTableName;

    @PostConstruct
    void init() {
        Log.info("=======================================");
        Log.info("FulfAI Partner API - Dev Bootstrap");
        Log.infof("Creating Company Table: %s", companyTableName);
        Log.infof("Creating Branch Table: %s", branchTableName);
        Log.infof("Creating Category Table: %s", categoryTableName);
        Log.infof("Creating Product Table: %s", productTableName);
        Log.infof("Creating Order Table: %s", orderTableName);
        Log.info("=======================================");

        TableCreator.createCompanyTable(dynamoDbClient, companyTableName);
        TableCreator.createBranchTable(dynamoDbClient, branchTableName);
        TableCreator.createCategoryTable(dynamoDbClient, categoryTableName);
        TableCreator.createProductTable(dynamoDbClient, productTableName);
        TableCreator.createOrderTable(dynamoDbClient, orderTableName);
    }
}
