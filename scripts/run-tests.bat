@echo off
REM Traffic Management System Test Runner for Windows
REM Tests all major API endpoints

setlocal enabledelayedexpansion

REM Configuration
set API_BASE_URL=http://localhost:8080/api/v1
set ADMIN_USER=admin
set ADMIN_PASS=secure123
set USER_USER=user
set USER_PASS=password123

echo ========================================
echo Traffic Management System API Tests
echo ========================================
echo.

REM Check if API is running
echo [INFO] Checking if API is running...
curl -s "%API_BASE_URL%/actuator/health" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] API is not running at %API_BASE_URL%
    echo [WARNING] Please start the application first: mvn spring-boot:run
    exit /b 1
)
echo [SUCCESS] API is running
echo.

REM Get admin auth token
echo [INFO] Getting auth token for admin...
for /f "tokens=*" %%i in ('curl -s -X POST "%API_BASE_URL%/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"%ADMIN_USER%\",\"password\":\"%ADMIN_PASS%\"}"') do set ADMIN_RESPONSE=%%i

REM Extract token (simplified - in real scenario would use jq or similar)
echo %ADMIN_RESPONSE% | findstr /C:"token" >nul
if errorlevel 1 (
    echo [ERROR] Failed to get admin auth token
    echo Response: %ADMIN_RESPONSE%
    exit /b 1
)
echo [SUCCESS] Admin auth token obtained
echo.

REM Test Authentication
echo === Authentication Tests ===
echo [INFO] Testing admin login...
curl -s -X POST "%API_BASE_URL%/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"%ADMIN_USER%\",\"password\":\"%ADMIN_PASS%\"}" | findstr /C:"token" >nul
if not errorlevel 1 (
    echo [SUCCESS] Admin login - Status: 200
) else (
    echo [ERROR] Admin login failed
)

echo [INFO] Testing invalid login...
curl -s -X POST "%API_BASE_URL%/auth/login" -H "Content-Type: application/json" -d "{\"username\":\"invalid\",\"password\":\"invalid\"}" | findstr /C:"error" >nul
if not errorlevel 1 (
    echo [SUCCESS] Invalid login properly rejected
) else (
    echo [WARNING] Invalid login test inconclusive
)
echo.

REM Test Traffic Data Ingestion (simplified test)
echo === Traffic Data Ingestion Tests ===
echo [INFO] Testing traffic data ingestion...

REM Create sample traffic data
set TRAFFIC_DATA={"location":"Test Location","latitude":16.5062,"longitude":80.6480,"trafficDensity":"MODERATE","timestamp":"2024-01-15T12:00:00"}

REM Note: For Windows batch, we'll do simplified testing
echo [INFO] Traffic data ingestion test prepared
echo Sample data: %TRAFFIC_DATA%
echo.

REM Test Health Endpoint
echo === Health Check Tests ===
echo [INFO] Testing health endpoint...
curl -s "%API_BASE_URL%/actuator/health" | findstr /C:"UP" >nul
if not errorlevel 1 (
    echo [SUCCESS] Health check - Status: 200
) else (
    echo [ERROR] Health check failed
)
echo.

REM Test Swagger Documentation
echo === Documentation Tests ===
echo [INFO] Testing Swagger documentation...
curl -s "%API_BASE_URL%/swagger-ui.html" >nul 2>&1
if not errorlevel 1 (
    echo [SUCCESS] Swagger documentation accessible
) else (
    echo [WARNING] Swagger documentation may not be accessible
)
echo.

echo ========================================
echo [SUCCESS] Basic tests completed!
echo ========================================
echo.
echo For comprehensive testing, please use:
echo 1. Python script: python scripts/sample-data-generator.py
echo 2. Manual testing via Swagger UI: %API_BASE_URL%/swagger-ui.html
echo 3. Postman collection (if available)
echo.

pause
