package com.fulfai.common.filter;

import io.quarkus.logging.Log;
import jakarta.json.Json;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GlobalResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) {
        // Add CORS headers to all responses
        responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers",
            "Content-Type, Authorization, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers, " +
            "X-Amz-Date, X-Api-Key, X-Amz-Security-Token, X-Amz-User-Agent");
        responseContext.getHeaders().putSingle("Access-Control-Max-Age", "3600");

        Log.debugf("RESPONSE: %s", responseContext.getStatus());
        if (responseContext.getEntity() != null) {
            Log.debugf("RESPONSE_BODY: %s", responseContext.getEntity());
        }

        if (responseContext.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
            responseContext.setEntity(Json.createObjectBuilder()
                    .add("error", "Unauthorized")
                    .add("message", "Authentication required")
                    .build());
            responseContext.getHeaders().putSingle("Content-Type", "application/json");
        }
    }
}
