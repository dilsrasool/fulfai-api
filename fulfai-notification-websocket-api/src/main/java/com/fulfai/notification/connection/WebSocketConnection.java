package com.fulfai.notification.connection;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

import java.time.Instant;

/**
 * Represents a WebSocket connection stored in DynamoDB.
 *
 * Table: FulfAI-{env}-WebSocketConnection
 * PK: connectionId
 * GSI: userSub-index (for querying connections by user)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
@RegisterForReflection
public class WebSocketConnection {

    /**
     * The unique connection ID provided by API Gateway
     */
    private String connectionId;

    /**
     * The Cognito user sub (if authenticated)
     */
    private String userSub;

    /**
     * The API Gateway domain name (for sending messages back)
     */
    private String domainName;

    /**
     * The API Gateway stage (e.g., "production", "dev")
     */
    private String stage;

    /**
     * When the connection was established
     */
    private Instant connectedAt;

    /**
     * TTL for automatic cleanup (Unix timestamp in seconds)
     * Set to 24 hours from connection time
     */
    private Long ttl;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("connectionId")
    public String getConnectionId() {
        return connectionId;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = "userSub-index")
    @DynamoDbAttribute("userSub")
    public String getUserSub() {
        return userSub;
    }

    @DynamoDbAttribute("domainName")
    public String getDomainName() {
        return domainName;
    }

    @DynamoDbAttribute("stage")
    public String getStage() {
        return stage;
    }

    @DynamoDbAttribute("connectedAt")
    public Instant getConnectedAt() {
        return connectedAt;
    }

    @DynamoDbAttribute("ttl")
    public Long getTtl() {
        return ttl;
    }

    /**
     * Get the callback URL for sending messages to this connection
     */
    public String getCallbackUrl() {
        return String.format("https://%s/%s", domainName, stage);
    }
}
