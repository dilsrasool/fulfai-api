# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build all modules (JVM)
mvn clean package

# Build all modules (Native)
mvn clean package -Dnative

# Build specific module
mvn clean package -pl fulfai-partner-api -am

# Run dev mode
cd fulfai-partner-api && mvn quarkus:dev

# Run tests
mvn test

# Run single test
mvn test -pl fulfai-partner-api -Dtest=HealthResourceTest
```

## Architecture

Multi-module Quarkus 3.23.0 project for AWS Lambda REST APIs with native build support.

```
fulfai-api/
├── pom.xml                        # Parent POM with shared config
├── postman/partner/               # Postman collection for Partner API
├── fulfai-common/                 # Shared code module
│   └── src/main/java/com/fulfai/common/
│       ├── dynamodb/
│       │   ├── ClientFactory.java       # DynamoDB Enhanced Client factory
│       │   ├── DynamoDBUtils.java       # Query execution with debug logging
│       │   └── TableCreator.java        # Table creation utilities
│       ├── filter/
│       │   ├── GlobalRequestFilter.java  # Request logging (DEBUG level)
│       │   └── GlobalResponseFilter.java # Response logging + CORS
│       ├── exception/
│       │   └── GlobalExceptionHandler.java # Global exception handling
│       └── security/
│           ├── LambdaIdentityProvider.java   # Lambda auth interface
│           ├── CognitoSecurityContext.java   # Request-scoped user context
│           └── CognitoUtils.java             # Cognito helper utilities
└── fulfai-partner-api/            # Partner API (Lambda deployable)
    ├── export_vars_stag.sh        # Staging env variables
    ├── export_vars_prod.sh        # Production env variables
    └── src/main/java/com/fulfai/partner/
        ├── PartnerApplication.java    # JAX-RS app (@ApplicationPath("/api/partner"))
        ├── HealthResource.java        # Health + /me endpoints
        ├── DevBootstrap.java          # Dev profile table creation
        ├── Schemas.java               # DynamoDB table schemas
        ├── company/                   # Company CRUD module
        │   ├── Company.java           # Entity
        │   ├── CompanyRequestDTO.java
        │   ├── CompanyResponseDTO.java
        │   ├── CompanyMapper.java     # MapStruct mapper
        │   ├── CompanyRepository.java
        │   ├── CompanyService.java
        │   └── CompanyResource.java   # REST endpoints
        └── security/
            └── PartnerSecurityProvider.java  # Cognito auth implementation
```

## Adding New API Modules

1. Copy `fulfai-partner-api` structure
2. Update artifactId and package names
3. Add module to parent pom.xml `<modules>` section
4. Depend on `fulfai-common` for shared code
5. Create security provider implementing `LambdaIdentityProvider`
6. Update port in `application-dev.properties` (e.g., 8083, 8084)

## Adding New Entity (CRUD)

1. Create entity class with `@DynamoDbBean` in `<module>/<entity>/`
2. Create `<Entity>RequestDTO.java` and `<Entity>ResponseDTO.java`
3. Create `<Entity>Mapper.java` (MapStruct interface)
4. Add schema to `Schemas.java`
5. Create `<Entity>Repository.java` using `DynamoDBUtils`
6. Create `<Entity>Service.java`
7. Create `<Entity>Resource.java`
8. Add table name to `application-dev.properties` and `export_vars_*.sh`
9. Add table creation to `DevBootstrap.java`

## Key Dependencies

- **Quarkus 3.23.0** with AWS Lambda REST extension
- **AWS SDK**: DynamoDB (enhanced), S3, Athena, Lambda
- **MapStruct 1.6.3 + Lombok 1.18.32** for DTOs
- **iText 7.2.5** for PDF generation
- **Testcontainers + LocalStack** for integration tests

## Profiles & Ports

- `dev`: LocalStack (localhost:4566), Partner API on port **8082**
- `prod`: AWS credentials via default chain
- `native`: GraalVM native image build

## Security

- All endpoints require Cognito authentication
- `PartnerSecurityProvider` extracts user sub from Lambda event
- Use `CognitoSecurityContext` to access current user info

## Testing with Postman

- Collection: `postman/partner/FulfAI-Partner-API.postman_collection.json`
- Base URL: `http://localhost:8082/_lambda_`
- All requests use Lambda proxy format with Cognito identity in `requestContext`

## DynamoDB Table Naming

Format: `FulfAI-<env>-<TableName>`
- Example: `FulfAI-dev-Company`, `FulfAI-staging-Company`, `FulfAI-prod-Company`

## Logging

- Request/Response logging at DEBUG level via `GlobalRequestFilter`/`GlobalResponseFilter`
- DynamoDB operations logged via `DynamoDBUtils` (DYNAMODB_GET, DYNAMODB_SCAN, etc.)
- Security auth logged via `PartnerSecurityProvider` (SECURITY_AUTH)
