package com.fulfai.sellingpartner;



import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.sellingpartner.infrastructure.bootstrap.MetricsTablesBootstrap;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.CORSConfiguration;
import software.amazon.awssdk.services.s3.model.CORSRule;

import software.amazon.awssdk.services.s3.model.PutBucketCorsRequest;


@IfBuildProfile("dev")
@Startup
@ApplicationScoped
public class DevBootstrap {

    /* =========================
       CLIENTS
    ========================= */

    @Inject
    DynamoDbClient dynamoDbClient;

    @Inject
    S3Client s3Client;

    /* =========================
       TABLE NAMES
    ========================= */

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

    @ConfigProperty(name = "account.table.name")
    String accountTableName;

    @ConfigProperty(name = "userCompanyRole.table.name")
    String userCompanyRoleTableName;

    @ConfigProperty(name = "companyJoinRequest.table.name")
    String companyJoinRequestTableName;

    /* =========================
       BUCKET NAMES
    ========================= */

    @ConfigProperty(name = "company.assets.bucket.name")
    String companyAssetsBucket;

    @ConfigProperty(name = "assets.bucket.name")
    String assetsBucket;

    @ConfigProperty(name = "datalake.bucket.name")
    String datalakeBucket;

    /* =========================
       INIT
    ========================= */

    @PostConstruct
    void init() {

        Log.info("=======================================");
        Log.info("FulfAI Selling Partner API - DEV BOOTSTRAP");
        Log.info("=======================================");

        /* ---------- DynamoDB Tables ---------- */

        Log.info("Initializing DynamoDB tables...");

        TableCreator.createCompanyTable(dynamoDbClient, companyTableName);
        TableCreator.createBranchTable(dynamoDbClient, branchTableName);
        TableCreator.createCategoryTable(dynamoDbClient, categoryTableName);
        TableCreator.createProductTable(dynamoDbClient, productTableName);
        TableCreator.createOrderTable(dynamoDbClient, orderTableName);
        TableCreator.createAccountTable(dynamoDbClient, accountTableName);
        TableCreator.createUserCompanyRoleTable(dynamoDbClient, userCompanyRoleTableName);
        TableCreator.createCompanyJoinRequestTable(dynamoDbClient, companyJoinRequestTableName);

        Log.info("DynamoDB tables ready");

        MetricsTablesBootstrap.createAll(dynamoDbClient);
        Log.info("Analytics tables ready");


        /* ---------- S3 Buckets ---------- */

        Log.info("Initializing S3 buckets...");

        createBucketIfMissing(companyAssetsBucket);
        createBucketIfMissing(assetsBucket);
        createBucketIfMissing(datalakeBucket);

        /* ---------- AUTO APPLY CORS (NEW) ---------- */

        applyCorsIfMissing(companyAssetsBucket);
        applyCorsIfMissing(assetsBucket);

        Log.info("S3 buckets + CORS ready");

        Log.info("=======================================");
        Log.info("DEV BOOTSTRAP COMPLETED");
        Log.info("=======================================");
    }

    /* =========================
       HELPERS
    ========================= */

    private void createBucketIfMissing(String bucketName) {
        try {
            s3Client.headBucket(
                    HeadBucketRequest.builder()
                            .bucket(bucketName)
                            .build()
            );
            Log.debugf("Bucket exists: %s", bucketName);
        } catch (NoSuchBucketException e) {
            Log.infof("Creating bucket: %s", bucketName);
            s3Client.createBucket(
                    CreateBucketRequest.builder()
                            .bucket(bucketName)
                            .build()
            );
        }
    }

    /* =========================
       CORS CONFIGURATION (NEW)
    ========================= */

    private void applyCorsIfMissing(String bucketName) {
    try {
        // Check if CORS already exists
        s3Client.getBucketCors(b -> b.bucket(bucketName));
        Log.debugf("CORS already configured for bucket: %s", bucketName);
    } catch (Exception e) {
        Log.infof("Applying CORS configuration to bucket: %s", bucketName);

        CORSRule rule = CORSRule.builder()
                .allowedMethods("GET", "PUT", "POST", "HEAD")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "http://localhost:8080"
                )
                .allowedHeaders("*")
                .exposeHeaders("ETag")
                .maxAgeSeconds(3000)
                .build();

        CORSConfiguration corsConfiguration = CORSConfiguration.builder()
                .corsRules(rule)
                .build();

        PutBucketCorsRequest request = PutBucketCorsRequest.builder()
                .bucket(bucketName)
                .corsConfiguration(corsConfiguration)
                .build();

        s3Client.putBucketCors(request);

        Log.infof("CORS successfully applied to bucket: %s", bucketName);
    }
}

}
