package com.fulfai.partner;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fulfai.common.security.CognitoSecurityContext;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @ConfigProperty(name = "env.name", defaultValue = "unknown")
    String envName;

    @Inject
    CognitoSecurityContext securityContext;

    @GET
    public Response health() {
        return Response.ok(Map.of(
            "status", "UP",
            "environment", envName,
            "service", "fulfai-partner-api"
        )).build();
    }

    @GET
    @Path("/me")
    @Authenticated
    public Response me() {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("authenticated", securityContext.isAuthenticated());
        userInfo.put("userSub", securityContext.getUserSub());
        userInfo.put("authType", securityContext.getAuthType());
        return Response.ok(userInfo).build();
    }
}
