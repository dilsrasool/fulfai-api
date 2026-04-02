package com.fulfai.sellingpartner;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

/**
 * Example usage of AuthenticatedApiClient for testing API endpoints.
 * This demonstrates how to authenticate and call protected backend APIs.
 */
public class ApiClientExample {

    public static void main(String[] args) {
        // Configuration - these should come from environment variables or config
        String cognitoClientId = System.getenv().getOrDefault("COGNITO_CLIENT_ID", "your-client-id");
        String cognitoUserPoolId = System.getenv().getOrDefault("COGNITO_USER_POOL_ID", "your-user-pool-id");
        String baseUrl = System.getenv().getOrDefault("API_BASE_URL", "http://localhost:8080");
        String username = System.getenv().getOrDefault("TEST_USERNAME", "test@example.com");
        String password = System.getenv().getOrDefault("TEST_PASSWORD", "password123");

        AuthenticatedApiClient client = new AuthenticatedApiClient(cognitoClientId, cognitoUserPoolId, baseUrl);

        try {
            // 1. Authenticate and get access token
            System.out.println("🔐 Authenticating user...");
            client.authenticate(username, password);

            // 2. Call protected API endpoints
            System.out.println("📡 Calling protected API endpoints...");

            // Example: Get user companies
            JsonNode companies = client.get("/api/selling-partner/company");
            System.out.println("Companies: " + companies);

            // Example: Create a company (if you have the right permissions)
            // Note: Adjust the payload according to your API requirements
            /*
            Map<String, Object> companyData = Map.of(
                "name", "Test Company",
                "address", "123 Test St",
                "city", "Test City",
                "country", "Test Country",
                "email", "test@company.com"
            );
            JsonNode createdCompany = client.post("/api/selling-partner/company", companyData);
            System.out.println("Created company: " + createdCompany);
            */

            // Example: Call public endpoint (no authentication required)
            System.out.println("🌐 Calling public endpoint...");
            JsonNode publicProducts = client.get("/api/selling-partner/public/products");
            System.out.println("Public products: " + publicProducts);

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 3. Logout
            client.logout();
        }
    }
}