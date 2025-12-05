#!/bin/sh

# Environment
export ENV="prod"
export QUARKUS_PROFILE="prod"

# DynamoDB Table Names
export COMPANY_TABLE_NAME="FulfAI-${ENV}-Company"
export BRANCH_TABLE_NAME="FulfAI-${ENV}-Branch"
export CATEGORY_TABLE_NAME="FulfAI-${ENV}-Category"
export PRODUCT_TABLE_NAME="FulfAI-${ENV}-Product"
export ORDER_TABLE_NAME="FulfAI-${ENV}-Order"

# S3 Buckets
export ASSETS_BUCKET_NAME="fulfai-${ENV}-assets"
export DATALAKE_BUCKET_NAME="fulfai-${ENV}-datalake"
export COMPANY_ASSETS_BUCKET_NAME="fulfai-${ENV}-company-assets"

# Logging
export LOG_LEVEL="ERROR"

echo "FulfAI Partner API - Production environment variables exported"
echo ""
echo "Environment: $ENV"
echo "Quarkus Profile: $QUARKUS_PROFILE"
echo ""
echo "DynamoDB Tables:"
echo "  COMPANY_TABLE_NAME: $COMPANY_TABLE_NAME"
echo "  BRANCH_TABLE_NAME: $BRANCH_TABLE_NAME"
echo "  CATEGORY_TABLE_NAME: $CATEGORY_TABLE_NAME"
echo "  PRODUCT_TABLE_NAME: $PRODUCT_TABLE_NAME"
echo "  ORDER_TABLE_NAME: $ORDER_TABLE_NAME"
echo ""
echo "S3 Buckets:"
echo "  ASSETS_BUCKET_NAME: $ASSETS_BUCKET_NAME"
echo "  DATALAKE_BUCKET_NAME: $DATALAKE_BUCKET_NAME"
echo "  COMPANY_ASSETS_BUCKET_NAME: $COMPANY_ASSETS_BUCKET_NAME"
echo ""
echo "Log Level: $LOG_LEVEL"
