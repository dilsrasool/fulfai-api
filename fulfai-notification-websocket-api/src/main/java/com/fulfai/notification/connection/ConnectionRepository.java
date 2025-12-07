package com.fulfai.notification.connection;

import com.fulfai.notification.dynamodb.ClientFactory;
import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for WebSocket connection persistence
 */
@ApplicationScoped
@RegisterForReflection
public class ConnectionRepository {

    @ConfigProperty(name = "websocket.connection.table.name")
    String tableName;

    @Inject
    ClientFactory clientFactory;

    private DynamoDbTable<WebSocketConnection> getTable() {
        return clientFactory.getEnhancedDynamoClient().table(tableName, Schemas.WEBSOCKET_CONNECTION_SCHEMA);
    }

    private DynamoDbIndex<WebSocketConnection> getUserSubIndex() {
        return getTable().index("userSub-index");
    }

    /**
     * Get a connection by its ID
     */
    public WebSocketConnection getById(String connectionId) {
        Log.debugf("Getting connection by id: %s", connectionId);
        Key key = Key.builder().partitionValue(connectionId).build();
        return getTable().getItem(key);
    }

    /**
     * Save a connection
     */
    public void save(WebSocketConnection connection) {
        Log.debugf("Saving connection: %s", connection.getConnectionId());
        getTable().putItem(connection);
    }

    /**
     * Delete a connection by its ID
     */
    public void delete(String connectionId) {
        Log.debugf("Deleting connection: %s", connectionId);
        Key key = Key.builder().partitionValue(connectionId).build();
        getTable().deleteItem(key);
    }

    /**
     * Get all connections for a user
     */
    public List<WebSocketConnection> getByUserSub(String userSub) {
        Log.debugf("Getting connections for userSub: %s", userSub);
        List<WebSocketConnection> connections = new ArrayList<>();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(userSub).build()
        );
        getUserSubIndex().query(queryConditional).forEach(page -> {
            connections.addAll(page.items());
        });
        return connections;
    }

    /**
     * Get table name (for infrastructure/debugging)
     */
    public String getTableName() {
        return tableName;
    }
}
