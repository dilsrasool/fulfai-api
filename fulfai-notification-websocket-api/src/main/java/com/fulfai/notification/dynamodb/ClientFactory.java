package com.fulfai.notification.dynamodb;

import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@ApplicationScoped
@RegisterForReflection
public class ClientFactory {
    @Inject
    DynamoDbClient dynamoDbClient;

    DynamoDbEnhancedClient enhancedClient;

    public DynamoDbEnhancedClient getEnhancedDynamoClient() {
        if (enhancedClient == null) {
            Log.debug("Creating a new DynamoDB Enhanced Client");
            enhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(dynamoDbClient)
                    .build();
        }
        return enhancedClient;
    }
}
