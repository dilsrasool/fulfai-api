# FulfAI API

Multi-module Quarkus project for FulfAI REST APIs, designed for AWS Lambda deployment with native build support.

## Project Structure

```
fulfai-api/
├── pom.xml                          # Parent POM with shared configuration
├── fulfai-common/                   # Shared code module
│   └── src/main/java/com/fulfai/common/
│       ├── dynamodb/                # DynamoDB utilities and client factory
│       ├── dto/                     # Common DTOs (PaginatedResponse, etc.)
│       ├── exception/               # Global exception handling
│       ├── filter/                  # Request/Response logging filters
│       ├── s3/                      # S3 utilities
│       └── security/                # Cognito authentication
├── fulfai-selling-partner-api/      # Selling Partner API (Lambda deployable)
│   ├── src/main/java/com/fulfai/sellingpartner/
│   │   ├── company/                 # Company CRUD
│   │   ├── branch/                  # Branch CRUD
│   │   ├── category/                # Category CRUD
│   │   ├── product/                 # Product CRUD
│   │   ├── order/                   # Order management
│   │   └── account/                 # Account balance tracking
│   └── src/main/resources/
│       ├── application.properties
│       └── application-dev.properties
└── fulfai-delivery-partner-api/     # Delivery Partner API (Lambda deployable)
    └── src/main/java/com/fulfai/deliverypartner/
```

## Prerequisites

### Required Software

| Software      | Version   | Installation                                      |
|---------------|-----------|---------------------------------------------------|
| Java JDK      | 17+       | `brew install openjdk@17` or [SDKMAN](https://sdkman.io/) |
| Maven         | 3.9+      | `brew install maven`                              |
| Docker        | 24+       | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |
| LocalStack    | Latest    | `brew install localstack` or via Docker           |
| AWS CLI       | 2.x       | `brew install awscli`                             |

### Verify Installation

```bash
java -version      # Should show Java 17+
mvn -version       # Should show Maven 3.9+
docker --version   # Should show Docker 24+
aws --version      # Should show aws-cli 2.x
```

## Technology Stack

| Technology                  | Version  | Purpose                           |
|-----------------------------|----------|-----------------------------------|
| Quarkus                     | 3.23.0   | Framework for Lambda REST APIs    |
| AWS SDK (DynamoDB, S3)      | 3.4.0    | AWS service integrations          |
| MapStruct                   | 1.6.3    | DTO mapping                       |
| Lombok                      | 1.18.32  | Boilerplate reduction             |
| Testcontainers + LocalStack | 1.12.57  | Integration testing               |

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd fulfai-api
```

### 2. Start LocalStack

LocalStack emulates AWS services locally. Start it using Docker:

```bash
docker run --rm -d \
  --name localstack \
  -p 4566:4566 \
  -e SERVICES=dynamodb,s3 \
  -e DEFAULT_REGION=us-east-1 \
  localstack/localstack
```

Or if you have LocalStack CLI installed:

```bash
localstack start -d
```

Verify LocalStack is running:

```bash
aws --endpoint-url=http://localhost:4566 dynamodb list-tables
```

### 3. Build the Project

```bash
# Build all modules
mvn clean install

# Build without tests
mvn clean install -DskipTests
```

### 4. Run in Development Mode

```bash
# Run Selling Partner API (port 8082)
mvn quarkus:dev -pl fulfai-selling-partner-api -am

# Run Delivery Partner API (port 8083)
mvn quarkus:dev -pl fulfai-delivery-partner-api -am
```

The `-am` flag builds dependent modules (fulfai-common) automatically.

### 5. Access the APIs

| API                    | Base URL                                    |
|------------------------|---------------------------------------------|
| Selling Partner API    | http://localhost:8082/_lambda_/api/selling-partner  |
| Delivery Partner API   | http://localhost:8083/_lambda_/api/delivery-partner |

Health check endpoints:

```bash
# Selling Partner API
curl http://localhost:8082/_lambda_/api/selling-partner/health

# Delivery Partner API
curl http://localhost:8083/_lambda_/api/delivery-partner/health
```

## Development

### Build Commands

```bash
# Build all modules (JVM)
mvn clean package

# Build all modules (Native - requires GraalVM)
mvn clean package -Dnative

# Build specific module
mvn clean package -pl fulfai-selling-partner-api -am

# Run tests
mvn test

# Run single test
mvn test -pl fulfai-selling-partner-api -Dtest=HealthResourceTest
```

### DynamoDB Tables

Tables are automatically created in dev mode by `DevBootstrap.java`. Table naming format:

```
FulfAI-<env>-<TableName>
```

Examples: `FulfAI-dev-Company`, `FulfAI-staging-Order`, `FulfAI-prod-Product`

### Profiles

| Profile | Description                              | Port  |
|---------|------------------------------------------|-------|
| dev     | LocalStack (localhost:4566)              | 8082/8083 |
| prod    | AWS credentials via default chain        | -     |
| native  | GraalVM native image build               | -     |

### Logging

- Request/Response logging at DEBUG level via `GlobalRequestFilter`/`GlobalResponseFilter`
- DynamoDB operations logged via `DynamoDBUtils`
- Set log levels in `application-dev.properties`

## Adding New Features

### Adding a New Entity (CRUD)

1. Create entity class with `@DynamoDbBean` in `<module>/<entity>/`
2. Create `<Entity>RequestDTO.java` and `<Entity>ResponseDTO.java`
3. Create `<Entity>Mapper.java` (MapStruct interface)
4. Add schema to `Schemas.java`
5. Create `<Entity>Repository.java` using `DynamoDBUtils`
6. Create `<Entity>Service.java`
7. Create `<Entity>Resource.java`
8. Add table name to `application-dev.properties` and `export_vars_*.sh`
9. Add table creation to `DevBootstrap.java` and `TableCreator.java`

### Adding a New API Module

1. Copy `fulfai-delivery-partner-api` structure
2. Update `artifactId` and package names in `pom.xml`
3. Add module to parent `pom.xml` `<modules>` section
4. Create Application class with unique `@ApplicationPath`
5. Update port in `application-dev.properties`

## Deployment

### Environment Variables

Source environment variables before deployment:

```bash
# Staging
source fulfai-selling-partner-api/export_vars_stag.sh

# Production
source fulfai-selling-partner-api/export_vars_prod.sh
```

### Native Build (for Lambda)

```bash
mvn clean package -Dnative -DskipTests
```

Requires GraalVM with native-image installed.

## Troubleshooting

### LocalStack Connection Issues

```bash
# Check if LocalStack is running
docker ps | grep localstack

# Check LocalStack logs
docker logs localstack

# Restart LocalStack
docker restart localstack
```

### Build Failures

```bash
# Clean and rebuild
mvn clean install -U

# Check Java version
java -version  # Must be 17+
```

### Port Already in Use

```bash
# Find process using port
lsof -i :8082

# Kill process
kill -9 <PID>
```

## API Documentation

### Selling Partner API Endpoints

| Method | Endpoint                                      | Description           |
|--------|-----------------------------------------------|-----------------------|
| GET    | /health                                       | Health check          |
| GET    | /health/me                                    | Current user info     |
| POST   | /company                                      | Create company        |
| GET    | /company/{id}                                 | Get company           |
| POST   | /company/{companyId}/branch                   | Create branch         |
| POST   | /company/{companyId}/branch/{branchId}/product| Create product       |
| POST   | /company/{companyId}/order                    | Create order          |
| GET    | /category                                     | List categories       |

### Delivery Partner API Endpoints

| Method | Endpoint  | Description    |
|--------|-----------|----------------|
| GET    | /health   | Health check   |
| GET    | /health/me| Current user info |

## Contributing

1. Create a feature branch from `main`
2. Make changes following existing code patterns
3. Write tests for new functionality
4. Run `mvn clean verify` before committing
5. Create a pull request with clear description

## License

Proprietary - All rights reserved
