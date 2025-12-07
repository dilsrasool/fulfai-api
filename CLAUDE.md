# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build all modules (JVM)
mvn clean package

# Build all modules (Native)
mvn clean package -Dnative

# Build specific module
mvn clean package -pl fulfai-selling-partner-api -am
mvn clean package -pl fulfai-notification-websocket-api -am -Dnative

# Run dev mode (specific module)
mvn quarkus:dev -pl fulfai-selling-partner-api -am
mvn quarkus:dev -pl fulfai-delivery-partner-api -am

# Run tests
mvn test

# Run single test
mvn test -pl fulfai-selling-partner-api -Dtest=HealthResourceTest
```

## Architecture

Multi-module Quarkus 3.23.0 project for AWS Lambda APIs with native build support.

```
fulfai-api/
├── pom.xml                           # Parent POM with shared config
├── fulfai-common-rest/               # Shared code for REST APIs
│   └── src/main/java/com/fulfai/common/
│       ├── dynamodb/                 # DynamoDB utilities
│       ├── dto/                      # Shared DTOs (PaginatedResponse)
│       ├── filter/                   # Request/Response filters
│       ├── exception/                # Global exception handling
│       └── security/                 # Cognito auth (CognitoSecurityProvider)
│
├── fulfai-selling-partner-api/       # Seller/Merchant API (port 8082)
│   └── src/main/java/com/fulfai/sellingpartner/
│       ├── company/                  # Company CRUD
│       ├── branch/                   # Branch CRUD
│       ├── category/                 # Category CRUD
│       ├── product/                  # Product CRUD
│       ├── order/                    # Order management
│       └── account/                  # Account management
│
├── fulfai-delivery-partner-api/      # Delivery/Driver API (port 8083)
│   └── src/main/java/com/fulfai/deliverypartner/
│       ├── company/                  # Delivery company CRUD
│       ├── driver/                   # Driver management
│       ├── assignment/               # Order-driver assignments
│       └── location/                 # GPS tracking + proximity search
│
└── fulfai-notification-websocket-api/  # WebSocket API (self-contained)
    └── src/main/java/com/fulfai/notification/
        ├── handler/                  # WebSocketHandler (Lambda entry point)
        ├── connection/               # WebSocketConnection entity, repository, service
        └── dynamodb/                 # Local ClientFactory (no fulfai-common-rest dependency)
```

## API Modules

### Selling Partner API (port 8082)
- **Package**: `com.fulfai.sellingpartner`
- **Path**: `/api/selling-partner`
- **Entities**: Company, Branch, Category, Product, Order, Account
- **Cognito Pool**: Seller pool

### Delivery Partner API (port 8083)
- **Package**: `com.fulfai.deliverypartner`
- **Path**: `/api/delivery-partner`
- **Entities**: Company, Driver, DriverOrderAssignment, DriverLocation
- **Cognito Pool**: Driver pool
- **Features**: Geohash-based proximity search for nearby drivers

### Notification WebSocket API
- **Package**: `com.fulfai.notification`
- **Lambda Handler**: `WebSocketHandler` (raw Lambda, not REST)
- **Routes**: `$connect` (IAM auth), `$disconnect`, `$default`, `sendMessage`
- **Self-contained**: Does not depend on fulfai-common-rest (avoids native build conflicts)
- **Auth**: Extracts user sub from `requestContext.identity.cognitoAuthenticationProvider`

## DynamoDB Tables

### Selling Partner API
| Table | GSIs |
|-------|------|
| FulfAI-{env}-Company | - |
| FulfAI-{env}-Branch | - |
| FulfAI-{env}-Category | - |
| FulfAI-{env}-Product | status-index |
| FulfAI-{env}-Order | status-index |
| FulfAI-{env}-Account | - |

### Delivery Partner API
| Table | GSIs |
|-------|------|
| FulfAI-{env}-DeliveryCompany | - |
| FulfAI-{env}-Driver | status-index |
| FulfAI-{env}-DriverAssignment | order-index, assignment-status-index |
| FulfAI-{env}-DriverLocation | geohash-index |

### Notification WebSocket API
| Table | GSIs |
|-------|------|
| FulfAI-{env}-WebSocketConnection | userSub-index |

## Adding New Entity (CRUD)

1. Create entity class with `@DynamoDbBean` in `<module>/<entity>/`
2. Create `<Entity>RequestDTO.java` and `<Entity>ResponseDTO.java`
3. Create `<Entity>Mapper.java` (MapStruct interface with `@Mapping(target=..., ignore=true)`)
4. Add schema to `Schemas.java`
5. Create `<Entity>Repository.java` using `DynamoDBUtils`
6. Create `<Entity>Service.java`
7. Create `<Entity>Resource.java`
8. Add table name to `application-dev.properties` and root `export_vars_*.sh`
9. Add table creation to `DevBootstrap.java` and `TableCreator.java`

## Key Dependencies

- **Quarkus 3.23.0** with AWS Lambda REST extension
- **AWS SDK**: DynamoDB (enhanced), S3, API Gateway Management API
- **MapStruct 1.6.3 + Lombok 1.18.32** for DTOs
- **Testcontainers + LocalStack** for integration tests

## Security

### REST APIs (Selling/Delivery Partner)
- Use `CognitoSecurityProvider` in fulfai-common-rest
- Extracts user sub from Lambda event `requestContext.identity.cognitoAuthenticationProvider`
- Use `CognitoSecurityContext` to access current user info

### WebSocket API (Notification)
- Uses IAM auth on `$connect` route
- Extracts user sub inline in `WebSocketHandler.extractUserSub()` from `cognitoAuthenticationProvider`
- No dependency on fulfai-common-rest to avoid native build conflicts with quarkus-amazon-lambda vs quarkus-amazon-lambda-rest

## Environment Variables

Centralized environment config files at project root (not per-module):

```bash
# Staging
source export_vars_stag.sh

# Production
source export_vars_prod.sh
```

These files export all variables for all APIs:
- DynamoDB table names (Selling Partner, Delivery Partner)
- S3 bucket names
- Cognito User Pool IDs
- Log level (DEBUG for staging, ERROR for prod)

## Logging

- Request/Response logging at DEBUG level via `GlobalRequestFilter`/`GlobalResponseFilter`
- DynamoDB operations logged via `DynamoDBUtils` (DYNAMODB_GET, DYNAMODB_QUERY, etc.)
- Set `quarkus.log.category."com.fulfai".level=DEBUG` for detailed logs
