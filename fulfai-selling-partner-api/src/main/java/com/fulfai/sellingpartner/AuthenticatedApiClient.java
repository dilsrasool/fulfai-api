package com.fulfai.sellingpartner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

/**
 * Authenticated API client for calling protected backend APIs.
 * Handles Cognito authentication and automatic token refresh.
 */
public class AuthenticatedApiClient {

    private final CognitoIdentityProviderClient cognitoClient;
    private final String clientId;
    private final String userPoolId;
    private final String baseUrl;
    private final String username;
    private final String password;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // Token cache
    private String accessToken;
    private String refreshToken;
    private Instant tokenExpiry;

    public AuthenticatedApiClient(String clientId, String userPoolId, String baseUrl, String username, String password) {
        this.clientId = clientId;
        this.userPoolId = userPoolId;
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.cognitoClient = CognitoIdentityProviderClient.create();
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public AuthenticatedApiClient(String clientId, String userPoolId, String baseUrl) {
        this(clientId, userPoolId, baseUrl, null, null);
    }

    /**
     * Authenticate user and obtain access token.
     */
    public void authenticate(String username, String password) throws IOException, InterruptedException {
        AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .userPoolId(userPoolId)
                .clientId(clientId)
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(
                        java.util.Map.of(
                                "USERNAME", username,
                                "PASSWORD", password
                        )
                )
                .build();

        AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);

        if (authResponse.challengeName() != null) {
            throw new RuntimeException("Authentication challenge required: " + authResponse.challengeName());
        }

        AuthenticationResultType authResult = authResponse.authenticationResult();
        this.accessToken = authResult.accessToken();
        this.refreshToken = authResult.refreshToken();

        // Calculate token expiry (access tokens typically expire in 1 hour)
        long expiresIn = authResult.expiresIn() != null ? authResult.expiresIn() : 3600;
        this.tokenExpiry = Instant.now().plusSeconds(expiresIn - 60); // Refresh 1 minute early

        System.out.println("✅ Authentication successful. Access token obtained.");
    }

    /**
     * Refresh the access token if it's expired or about to expire.
     */
    /**
     * Refresh the access token if it's expired or about to expire (or if no session exists).
     *
     * If no session exists, it will attempt to authenticate with the provided username/password.
     */
    private void refreshTokenIfNeeded() throws IOException, InterruptedException {
        if (accessToken == null || refreshToken == null || tokenExpiry == null) {
            if (username != null && password != null) {
                authenticate(username, password);
                return;
            }
            throw new IllegalStateException("No valid session. Please authenticate first.");
        }

        if (Instant.now().isAfter(tokenExpiry)) {
            System.out.println("🔄 Refreshing access token...");

            AdminInitiateAuthRequest refreshRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(clientId)
                    .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                    .authParameters(
                            java.util.Map.of("REFRESH_TOKEN", refreshToken)
                    )
                    .build();

            AdminInitiateAuthResponse refreshResponse = cognitoClient.adminInitiateAuth(refreshRequest);
            AuthenticationResultType authResult = refreshResponse.authenticationResult();

            this.accessToken = authResult.accessToken();

            // Calculate new expiry
            long expiresIn = authResult.expiresIn() != null ? authResult.expiresIn() : 3600;
            this.tokenExpiry = Instant.now().plusSeconds(expiresIn - 60);

            System.out.println("✅ Token refreshed successfully.");
        }
    }

    /**
     * Make an authenticated GET request to a protected API endpoint.
     */
    public JsonNode get(String path) throws IOException, InterruptedException {
        return makeRequest("GET", path, null);
    }

    /**
     * Make an authenticated POST request to a protected API endpoint.
     */
    public JsonNode post(String path, Object body) throws IOException, InterruptedException {
        return makeRequest("POST", path, body);
    }

    /**
     * Make an authenticated PUT request to a protected API endpoint.
     */
    public JsonNode put(String path, Object body) throws IOException, InterruptedException {
        return makeRequest("PUT", path, body);
    }

    /**
     * Make an authenticated DELETE request to a protected API endpoint.
     */
    public JsonNode delete(String path) throws IOException, InterruptedException {
        return makeRequest("DELETE", path, null);
    }

    /**
     * Make an authenticated HTTP request with automatic token refresh.
     */
    private JsonNode makeRequest(String method, String path, Object body) throws IOException, InterruptedException {
        // Skip authentication for public endpoints
        if (path.startsWith("/api/selling-partner/public/")) {
            HttpResponse<String> response = makeHttpRequest(method, path, body, null);
            return handleResponse(response);
        }

        // Ensure we have a valid token
        refreshTokenIfNeeded();

        // Make the request with Authorization header
        HttpResponse<String> response = makeHttpRequest(method, path, body, accessToken);

        // Handle 401 - Invalid token
        if (response.statusCode() == 401) {
            String responseBody = response.body();
            if (responseBody.contains("Invalid Cognito token") || responseBody.contains("401")) {
                System.out.println("⚠️  Token invalid or expired. Attempting refresh and retry...");

                // Force token refresh
                tokenExpiry = Instant.now().minusSeconds(1);
                refreshTokenIfNeeded();

                // Retry the request
                response = makeHttpRequest(method, path, body, accessToken);
            }
        }

        return handleResponse(response);
    }

    /**
     * Handle HTTP response and parse JSON.
     */
    private JsonNode handleResponse(HttpResponse<String> response) throws IOException {
        // Check for success
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readTree(response.body());
        } else {
            throw new RuntimeException("API request failed: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * Make the actual HTTP request.
     */
    private HttpResponse<String> makeHttpRequest(String method, String path, Object body, String authToken)
            throws IOException, InterruptedException {

        String url = baseUrl + path;
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json");

        // Add Authorization header for protected endpoints
        if (authToken != null) {
            requestBuilder.header("Authorization", "Bearer " + authToken);
        }

        // Add body for POST/PUT requests
        if (body != null && ("POST".equals(method) || "PUT".equals(method))) {
            String jsonBody = objectMapper.writeValueAsString(body);
            requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpRequest request = requestBuilder.build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Check if the client is authenticated.
     */
    public boolean isAuthenticated() {
        return accessToken != null && refreshToken != null &&
               tokenExpiry != null && Instant.now().isBefore(tokenExpiry);
    }

    /**
     * Logout and clear tokens.
     */
    public void logout() {
        this.accessToken = null;
        this.refreshToken = null;
        this.tokenExpiry = null;
        System.out.println("👋 Logged out successfully.");
    }
}