@echo off
REM Traffic Management System Startup Script for Windows

echo ========================================
echo Traffic Management System Startup
echo ========================================
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install Java 17 or higher and try again
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven is not installed or not in PATH
    echo Please install Maven and try again
    pause
    exit /b 1
)

echo [SUCCESS] Java and Maven are available
echo.

REM Build the application
echo [INFO] Building the application...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [ERROR] Build failed
    pause
    exit /b 1
)

echo [SUCCESS] Application built successfully
echo.

REM Start the application
echo [INFO] Starting Traffic Management System...
echo [INFO] The application will be available at: http://localhost:8080/api/v1
echo [INFO] Swagger documentation: http://localhost:8080/api/v1/swagger-ui.html
echo [INFO] H2 Console: http://localhost:8080/api/v1/h2-console
echo.
echo [INFO] Press Ctrl+C to stop the application
echo.

call mvn spring-boot:run

echo.
echo [INFO] Application stopped
pause
