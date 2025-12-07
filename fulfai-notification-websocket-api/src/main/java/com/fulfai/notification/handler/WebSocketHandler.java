package com.fulfai.notification.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fulfai.notification.connection.ConnectionService;
import io.quarkus.logging.Log;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.HashMap;
import java.util.Map;

/**
 * Main Lambda handler for WebSocket API Gateway.
 * Routes requests to appropriate handlers based on routeKey ($connect, $disconnect, $default, custom routes).
 * Manually populates security context from the WebSocket request context.
 */
@Named("websocket")
@RegisterForReflection
public class WebSocketHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    @Inject
    ConnectionService connectionService;

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        Log.debugf("WebSocket event received: %s", event);

        try {
            // Extract request context
            @SuppressWarnings("unchecked")
            Map<String, Object> requestContext = (Map<String, Object>) event.get("requestContext");
            if (requestContext == null) {
                return errorResponse(400, "Missing requestContext");
            }

            String routeKey = (String) requestContext.get("routeKey");
            String connectionId = (String) requestContext.get("connectionId");
            String domainName = (String) requestContext.get("domainName");
            String stage = (String) requestContext.get("stage");

            Log.infof("WebSocket request: routeKey=%s, connectionId=%s", routeKey, connectionId);

            // Extract user sub from IAM authentication context
            String userSub = extractUserSub(requestContext);
            Log.debugf("Extracted userSub: %s", userSub);

            // Route to appropriate handler
            switch (routeKey) {
                case "$connect":
                    return handleConnect(connectionId, userSub, domainName, stage);
                case "$disconnect":
                    return handleDisconnect(connectionId);
                case "$default":
                    return handleDefault(connectionId, event);
                default:
                    // Custom route - handle as message
                    return handleMessage(connectionId, routeKey, event);
            }

        } catch (Exception e) {
            Log.errorf(e, "Error handling WebSocket request");
            return errorResponse(500, "Internal server error: " + e.getMessage());
        }
    }

    /**
     * Extract user sub from IAM authentication context.
     * For WebSocket with IAM auth, the identity info is in requestContext.identity
     */
    private String extractUserSub(Map<String, Object> requestContext) {
        @SuppressWarnings("unchecked")
        Map<String, Object> identity = (Map<String, Object>) requestContext.get("identity");
        if (identity == null) {
            Log.debug("No identity in requestContext");
            return null;
        }

        // For IAM auth, cognitoAuthenticationProvider contains the user info
        // Format: "cognito-idp.region.amazonaws.com/poolId,cognito-idp.region.amazonaws.com/poolId:CognitoSignIn:sub"
        String cognitoAuthProvider = (String) identity.get("cognitoAuthenticationProvider");
        if (cognitoAuthProvider != null) {
            int index = cognitoAuthProvider.indexOf("CognitoSignIn:");
            if (index != -1) {
                return cognitoAuthProvider.substring(index + "CognitoSignIn:".length());
            }
        }

        // Fallback: try to get userArn or caller identity
        String userArn = (String) identity.get("userArn");
        if (userArn != null) {
            Log.debugf("Using userArn as identifier: %s", userArn);
            // Extract the assumed role session name which often contains the Cognito identity
            // Format: arn:aws:sts::account:assumed-role/role-name/CognitoIdentityCredentials
            int lastSlash = userArn.lastIndexOf('/');
            if (lastSlash > 0) {
                return userArn.substring(lastSlash + 1);
            }
        }

        return null;
    }

    private Map<String, Object> handleConnect(String connectionId, String userSub, String domainName, String stage) {
        Log.infof("Handling $connect: connectionId=%s, userSub=%s", connectionId, userSub);

        try {
            connectionService.saveConnection(connectionId, userSub, domainName, stage);
            return successResponse("Connected");
        } catch (Exception e) {
            Log.errorf(e, "Error saving connection");
            return errorResponse(500, "Failed to save connection");
        }
    }

    private Map<String, Object> handleDisconnect(String connectionId) {
        Log.infof("Handling $disconnect: connectionId=%s", connectionId);

        try {
            connectionService.deleteConnection(connectionId);
            return successResponse("Disconnected");
        } catch (Exception e) {
            Log.errorf(e, "Error deleting connection");
            // Return success anyway - client is already gone
            return successResponse("Disconnected");
        }
    }

    private Map<String, Object> handleDefault(String connectionId, Map<String, Object> event) {
        Log.infof("Handling $default: connectionId=%s", connectionId);
        String body = (String) event.get("body");
        Log.debugf("Message body: %s", body);

        // Echo back for now - can be extended for custom message handling
        return successResponse("Message received");
    }

    private Map<String, Object> handleMessage(String connectionId, String routeKey, Map<String, Object> event) {
        Log.infof("Handling custom route: routeKey=%s, connectionId=%s", routeKey, connectionId);
        String body = (String) event.get("body");
        Log.debugf("Message body: %s", body);

        // Handle custom routes here
        return successResponse("Route handled: " + routeKey);
    }

    private Map<String, Object> successResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("body", message);
        return response;
    }

    private Map<String, Object> errorResponse(int statusCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", statusCode);
        response.put("body", message);
        return response;
    }
}
