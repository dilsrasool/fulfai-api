package com.fulfai.sellingpartner.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

/**
 * Resolves frontend-facing identifiers (email)
 * into Cognito internal identifiers (sub).
 *
 * This class intentionally hides Cognito implementation
 * details from business logic.
 */
@ApplicationScoped
public class CognitoUserResolver {

    /**
     * Resolve Cognito sub by email.
     *
     * Current behavior (DEV / STUB):
     * - Uses deterministic placeholder "EMAIL#{email}"
     *
     * Future behavior (PROD):
     * - Use Cognito AdminGetUser / ListUsers
     * - Create user if not found (invite flow)
     */
    public String getSubByEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new NotFoundException("User email is required");
        }

        // Normalize email
        String normalizedEmail = email.trim().toLowerCase();

        /*
         * TEMPORARY STRATEGY (SAFE):
         * - Allows invite-by-email before signup
         * - Deterministic mapping
         * - No data leakage
         *
         * REPLACE LATER WITH:
         *   cognito.adminGetUser(...)
         *   or adminCreateUser(...)
         */
        return "EMAIL#" + normalizedEmail;
    }

    /**
     * Resolve or create Cognito user by email.
     *
     * Alias method used by invite flow.
     * Keeps service code clean and future-proof.
     */
    public String getOrCreateUser(String email) {
        return getSubByEmail(email);
    }
}
