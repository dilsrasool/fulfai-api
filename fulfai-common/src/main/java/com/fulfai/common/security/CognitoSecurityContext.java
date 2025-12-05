package com.fulfai.common.security;

import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

/**
 * Request-scoped security context for Cognito authenticated users.
 * Provides easy access to current user information.
 */
@RequestScoped
public class CognitoSecurityContext {

    @Inject
    SecurityIdentity securityIdentity;

    /**
     * Get the Cognito user sub (unique user identifier)
     */
    public String getUserSub() {
        if (securityIdentity == null || securityIdentity.getPrincipal() == null) {
            return null;
        }
        return securityIdentity.getPrincipal().getName();
    }

    /**
     * Check if the current request is authenticated
     */
    public boolean isAuthenticated() {
        return securityIdentity != null && !securityIdentity.isAnonymous();
    }

    /**
     * Get authentication type (e.g., "authenticated" from Cognito)
     */
    public String getAuthType() {
        return getAttribute("auth_type", String.class);
    }

    /**
     * Get a specific attribute from the security identity
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name, Class<T> type) {
        if (securityIdentity == null) {
            return null;
        }
        Object value = securityIdentity.getAttribute(name);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String role) {
        return securityIdentity != null && securityIdentity.hasRole(role);
    }

    /**
     * Log current security context (for debugging)
     */
    public void logContext() {
        Log.debugf("SECURITY_CONTEXT: authenticated=%s, userSub=%s, authType=%s",
                isAuthenticated(), getUserSub(), getAuthType());
    }
}
