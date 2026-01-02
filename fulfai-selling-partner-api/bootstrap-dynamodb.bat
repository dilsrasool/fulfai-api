@echo off
SETLOCAL

echo ==========================================
echo  FulfAI LocalStack DynamoDB Bootstrap
echo ==========================================

SET ENDPOINT=http://localhost:4566
SET REGION=us-east-1

echo.
echo 👉 Inserting Company (id + ownerSub)...
aws dynamodb put-item ^
  --endpoint-url %ENDPOINT% ^
  --region %REGION% ^
  --table-name FulfAI-dev-Company ^
  --item file://company.json

echo.
echo 👉 Assigning User Role (OWNER)...
aws dynamodb put-item ^
  --endpoint-url %ENDPOINT% ^
  --region %REGION% ^
  --table-name FulfAI-dev-UserCompanyRole ^
  --item file://user-company-role.json

echo.
echo ✅ Bootstrap completed successfully!
echo You can now call: GET /api/selling-partner/company/me
echo.

ENDLOCAL
pause
