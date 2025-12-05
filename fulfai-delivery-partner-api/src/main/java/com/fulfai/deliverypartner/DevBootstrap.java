package com.fulfai.deliverypartner;

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

    @ConfigProperty(name = "delivery.company.table.name")
    String companyTableName;

    @ConfigProperty(name = "delivery.driver.table.name")
    String driverTableName;

    @ConfigProperty(name = "delivery.assignment.table.name")
    String assignmentTableName;

    @ConfigProperty(name = "delivery.location.table.name")
    String locationTableName;

    @PostConstruct
    void init() {
        Log.info("=======================================");
        Log.info("FulfAI Delivery Partner API - Dev Bootstrap");
        Log.infof("Creating Company Table: %s", companyTableName);
        Log.infof("Creating Driver Table: %s", driverTableName);
        Log.infof("Creating Assignment Table: %s", assignmentTableName);
        Log.infof("Creating Location Table: %s", locationTableName);
        Log.info("=======================================");

        TableCreator.createCompanyTable(dynamoDbClient, companyTableName);
        TableCreator.createDriverTable(dynamoDbClient, driverTableName);
        TableCreator.createAssignmentTable(dynamoDbClient, assignmentTableName);
        TableCreator.createLocationTable(dynamoDbClient, locationTableName);
    }
}
