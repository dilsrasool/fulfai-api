package com.fulfai.notification.connection;

import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service for managing WebSocket connections
 */
@ApplicationScoped
@RegisterForReflection
public class ConnectionService {

    private static final long TTL_HOURS = 24; // Connections expire after 24 hours

    @Inject
    ConnectionRepository connectionRepository;

    /**
     * Save a new WebSocket connection
     *
     * @param connectionId The API Gateway connection ID
     * @param userSub      The Cognito user sub (may be null for unauthenticated)
     * @param domainName   The API Gateway domain name
     * @param stage        The API Gateway stage
     */
    public void saveConnection(String connectionId, String userSub, String domainName, String stage) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(TTL_HOURS, ChronoUnit.HOURS);

        WebSocketConnection connection = WebSocketConnection.builder()
                .connectionId(connectionId)
                .userSub(userSub)
                .domainName(domainName)
                .stage(stage)
                .connectedAt(now)
                .ttl(expiresAt.getEpochSecond())
                .build();

        connectionRepository.save(connection);
        Log.infof("Saved connection: connectionId=%s, userSub=%s", connectionId, userSub);
    }

    /**
     * Delete a WebSocket connection
     *
     * @param connectionId The API Gateway connection ID
     */
    public void deleteConnection(String connectionId) {
        connectionRepository.delete(connectionId);
        Log.infof("Deleted connection: connectionId=%s", connectionId);
    }

    /**
     * Get a connection by ID
     *
     * @param connectionId The API Gateway connection ID
     * @return The connection or null if not found
     */
    public WebSocketConnection getConnection(String connectionId) {
        return connectionRepository.getById(connectionId);
    }

    /**
     * Get all connections for a user
     *
     * @param userSub The Cognito user sub
     * @return List of connections for the user
     */
    public List<WebSocketConnection> getConnectionsForUser(String userSub) {
        return connectionRepository.getByUserSub(userSub);
    }
}
