#!/bin/sh

# =============================================================================
# FulfAI API - Staging Environment Variables
# =============================================================================
# Source this file before running any API in staging:
#   source export_vars_stag.sh
# =============================================================================

# Environment
export ENV="staging"
export QUARKUS_PROFILE="prod"
export AWS_REGION="me-central-1"

# -----------------------------------------------------------------------------
# Selling Partner API - DynamoDB Tables
# -----------------------------------------------------------------------------
export COMPANY_TABLE_NAME="FulfAI-${ENV}-Company"
export BRANCH_TABLE_NAME="FulfAI-${ENV}-Branch"
export CATEGORY_TABLE_NAME="FulfAI-${ENV}-Category"
export PRODUCT_TABLE_NAME="FulfAI-${ENV}-Product"
export ORDER_TABLE_NAME="FulfAI-${ENV}-Order"
export ACCOUNT_TABLE_NAME="FulfAI-${ENV}-Account"

# -----------------------------------------------------------------------------
# Delivery Partner API - DynamoDB Tables
# -----------------------------------------------------------------------------
export DELIVERY_COMPANY_TABLE_NAME="FulfAI-${ENV}-DeliveryCompany"
export DELIVERY_DRIVER_TABLE_NAME="FulfAI-${ENV}-Driver"
export DELIVERY_ASSIGNMENT_TABLE_NAME="FulfAI-${ENV}-DriverAssignment"
export DELIVERY_LOCATION_TABLE_NAME="FulfAI-${ENV}-DriverLocation"

# -----------------------------------------------------------------------------
# Notification WebSocket API - DynamoDB Tables
# -----------------------------------------------------------------------------
export WEBSOCKET_CONNECTION_TABLE_NAME="FulfAI-${ENV}-WebSocketConnection"

# -----------------------------------------------------------------------------
# S3 Buckets
# -----------------------------------------------------------------------------
export ASSETS_BUCKET_NAME="fulfai-${ENV}-assets"
export DATALAKE_BUCKET_NAME="fulfai-${ENV}-datalake"
export COMPANY_ASSETS_BUCKET_NAME="fulfai-${ENV}-company-assets"

# -----------------------------------------------------------------------------
# Cognito User Pools
# -----------------------------------------------------------------------------
export SELLER_USER_POOL_ID="me-central-1_xxxxx"
export DRIVER_USER_POOL_ID="me-central-1_yyyyy"

# -----------------------------------------------------------------------------
# Logging
# -----------------------------------------------------------------------------
export LOG_LEVEL="DEBUG"

# =============================================================================
# Summary
# =============================================================================
echo "FulfAI API - Staging environment variables exported"
echo ""
echo "Environment: $ENV"
echo "Quarkus Profile: $QUARKUS_PROFILE"
echo "AWS Region: $AWS_REGION"
echo "Log Level: $LOG_LEVEL"
echo ""
echo "Selling Partner API Tables:"
echo "  COMPANY_TABLE_NAME: $COMPANY_TABLE_NAME"
echo "  BRANCH_TABLE_NAME: $BRANCH_TABLE_NAME"
echo "  CATEGORY_TABLE_NAME: $CATEGORY_TABLE_NAME"
echo "  PRODUCT_TABLE_NAME: $PRODUCT_TABLE_NAME"
echo "  ORDER_TABLE_NAME: $ORDER_TABLE_NAME"
echo "  ACCOUNT_TABLE_NAME: $ACCOUNT_TABLE_NAME"
echo ""
echo "Delivery Partner API Tables:"
echo "  DELIVERY_COMPANY_TABLE_NAME: $DELIVERY_COMPANY_TABLE_NAME"
echo "  DELIVERY_DRIVER_TABLE_NAME: $DELIVERY_DRIVER_TABLE_NAME"
echo "  DELIVERY_ASSIGNMENT_TABLE_NAME: $DELIVERY_ASSIGNMENT_TABLE_NAME"
echo "  DELIVERY_LOCATION_TABLE_NAME: $DELIVERY_LOCATION_TABLE_NAME"
echo ""
echo "Notification WebSocket API Tables:"
echo "  WEBSOCKET_CONNECTION_TABLE_NAME: $WEBSOCKET_CONNECTION_TABLE_NAME"
echo ""
echo "S3 Buckets:"
echo "  ASSETS_BUCKET_NAME: $ASSETS_BUCKET_NAME"
echo "  DATALAKE_BUCKET_NAME: $DATALAKE_BUCKET_NAME"
echo "  COMPANY_ASSETS_BUCKET_NAME: $COMPANY_ASSETS_BUCKET_NAME"
