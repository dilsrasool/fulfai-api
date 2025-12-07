package com.fulfai.notification.connection;

import io.quarkus.runtime.annotations.RegisterForReflection;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;

import java.time.Instant;

/**
 * DynamoDB schema definitions for WebSocket notification entities
 */
@RegisterForReflection
public class Schemas {

    public static final TableSchema<WebSocketConnection> WEBSOCKET_CONNECTION_SCHEMA = TableSchema.builder(WebSocketConnection.class)
            .newItemSupplier(WebSocketConnection::new)
            .addAttribute(String.class, a -> a.name("connectionId")
                    .getter(WebSocketConnection::getConnectionId)
                    .setter(WebSocketConnection::setConnectionId)
                    .tags(StaticAttributeTags.primaryPartitionKey()))
            .addAttribute(String.class, a -> a.name("userSub")
                    .getter(WebSocketConnection::getUserSub)
                    .setter(WebSocketConnection::setUserSub)
                    .tags(StaticAttributeTags.secondaryPartitionKey("userSub-index")))
            .addAttribute(String.class, a -> a.name("domainName")
                    .getter(WebSocketConnection::getDomainName)
                    .setter(WebSocketConnection::setDomainName))
            .addAttribute(String.class, a -> a.name("stage")
                    .getter(WebSocketConnection::getStage)
                    .setter(WebSocketConnection::setStage))
            .addAttribute(Instant.class, a -> a.name("connectedAt")
                    .getter(WebSocketConnection::getConnectedAt)
                    .setter(WebSocketConnection::setConnectedAt))
            .addAttribute(Long.class, a -> a.name("ttl")
                    .getter(WebSocketConnection::getTtl)
                    .setter(WebSocketConnection::setTtl))
            .build();
}
