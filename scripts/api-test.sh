#!/bin/bash

# Traffic Management System API Test Script
# Tests all major API endpoints

set -e

# Configuration
API_BASE_URL="http://localhost:8080/api/v1"
ADMIN_USER="admin"
ADMIN_PASS="secure123"
USER_USER="user"
USER_PASS="password123"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Function to get auth token
get_auth_token() {
    local username=$1
    local password=$2
    
    print_status "Getting auth token for user: $username"
    
    local response=$(curl -s -X POST "$API_BASE_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"$username\",\"password\":\"$password\"}")
    
    local token=$(echo $response | jq -r '.token // empty')
    
    if [ -z "$token" ] || [ "$token" = "null" ]; then
        print_error "Failed to get auth token for $username"
        echo "Response: $response"
        exit 1
    fi
    
    print_success "Auth token obtained for $username"
    echo $token
}

# Function to test API endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local token=$3
    local data=$4
    local expected_status=$5
    local description=$6
    
    print_status "Testing: $description"
    
    local curl_cmd="curl -s -w '%{http_code}' -X $method '$API_BASE_URL$endpoint'"
    
    if [ ! -z "$token" ]; then
        curl_cmd="$curl_cmd -H 'Authorization: Bearer $token'"
    fi
    
    if [ ! -z "$data" ]; then
        curl_cmd="$curl_cmd -H 'Content-Type: application/json' -d '$data'"
    fi
    
    local response=$(eval $curl_cmd)
    local status_code="${response: -3}"
    local body="${response%???}"
    
    if [ "$status_code" = "$expected_status" ]; then
        print_success "$description - Status: $status_code"
        if [ ! -z "$body" ] && [ "$body" != "" ]; then
            echo "Response: $(echo $body | jq . 2>/dev/null || echo $body)"
        fi
    else
        print_error "$description - Expected: $expected_status, Got: $status_code"
        echo "Response: $body"
    fi
    
    echo ""
}

# Main test function
run_tests() {
    echo "========================================"
    echo "Traffic Management System API Tests"
    echo "========================================"
    echo ""
    
    # Check if API is running
    print_status "Checking if API is running..."
    if ! curl -s "$API_BASE_URL/actuator/health" > /dev/null; then
        print_error "API is not running at $API_BASE_URL"
        print_warning "Please start the application first: mvn spring-boot:run"
        exit 1
    fi
    print_success "API is running"
    echo ""
    
    # Get auth tokens
    ADMIN_TOKEN=$(get_auth_token $ADMIN_USER $ADMIN_PASS)
    USER_TOKEN=$(get_auth_token $USER_USER $USER_PASS)
    echo ""
    
    # Test Authentication Endpoints
    echo "=== Authentication Tests ==="
    
    test_endpoint "POST" "/auth/login" "" \
        "{\"username\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASS\"}" \
        "200" "Admin login"
    
    test_endpoint "POST" "/auth/login" "" \
        "{\"username\":\"invalid\",\"password\":\"invalid\"}" \
        "500" "Invalid login"
    
    test_endpoint "POST" "/auth/validate" "" "" "400" "Token validation without token"
    
    # Test Traffic Data Ingestion
    echo "=== Traffic Data Ingestion Tests ==="
    
    local traffic_data='{
        "location": "Test Location",
        "latitude": 16.5062,
        "longitude": 80.6480,
        "trafficDensity": "MODERATE",
        "timestamp": "'$(date -u +"%Y-%m-%dT%H:%M:%S")'"
    }'
    
    test_endpoint "POST" "/traffic/ingest" "$ADMIN_TOKEN" "$traffic_data" \
        "201" "Ingest traffic data (Admin)"
    
    test_endpoint "POST" "/traffic/ingest" "$USER_TOKEN" "$traffic_data" \
        "403" "Ingest traffic data (User - should fail)"
    
    test_endpoint "POST" "/traffic/ingest" "" "$traffic_data" \
        "401" "Ingest traffic data (No auth - should fail)"
    
    # Test Traffic Prediction
    echo "=== Traffic Prediction Tests ==="
    
    test_endpoint "GET" "/traffic/predict?lat=16.5062&lon=80.6480&timestamp=$(date -u +"%Y-%m-%dT%H:%M:%S")" \
        "$USER_TOKEN" "" "200" "Traffic prediction"
    
    test_endpoint "GET" "/traffic/predict?lat=16.5062" "$USER_TOKEN" "" \
        "400" "Traffic prediction (missing parameters)"
    
    # Test Route Recommendation
    echo "=== Route Recommendation Tests ==="
    
    test_endpoint "GET" "/traffic/route?source=Vijayawada%20Junction&destination=PNBS%20Bus%20Stand&eco=true" \
        "$USER_TOKEN" "" "200" "Route recommendation"
    
    test_endpoint "GET" "/traffic/route?source=Test" "$USER_TOKEN" "" \
        "400" "Route recommendation (missing destination)"
    
    # Test Signal Optimization
    echo "=== Signal Optimization Tests ==="
    
    local signal_data='{
        "intersectionId": "INT-TEST-001",
        "north": 50,
        "south": 30,
        "east": 20,
        "west": 25
    }'
    
    test_endpoint "POST" "/traffic/signal/optimize" "$ADMIN_TOKEN" "$signal_data" \
        "200" "Signal optimization (Admin)"
    
    test_endpoint "POST" "/traffic/signal/optimize" "$USER_TOKEN" "$signal_data" \
        "403" "Signal optimization (User - should fail)"
    
    # Test Traffic Trends
    echo "=== Traffic Trends Tests ==="
    
    test_endpoint "GET" "/traffic/trends?location=Vijayawada&period=monthly" \
        "$USER_TOKEN" "" "200" "Traffic trends"
    
    test_endpoint "GET" "/traffic/trends?location=NonExistent&period=daily" \
        "$USER_TOKEN" "" "200" "Traffic trends (non-existent location)"
    
    # Test Model Training
    echo "=== Model Training Tests ==="
    
    test_endpoint "POST" "/traffic/train" "$ADMIN_TOKEN" "" \
        "200" "Model training (Admin)"
    
    test_endpoint "POST" "/traffic/train" "$USER_TOKEN" "" \
        "403" "Model training (User - should fail)"
    
    # Test Data Retrieval
    echo "=== Data Retrieval Tests ==="
    
    test_endpoint "GET" "/traffic/data?limit=5" "$USER_TOKEN" "" \
        "200" "Get traffic data"
    
    test_endpoint "GET" "/traffic/data?location=Test&limit=10" "$USER_TOKEN" "" \
        "200" "Get traffic data by location"
    
    # Test Health Endpoints
    echo "=== Health Check Tests ==="
    
    test_endpoint "GET" "/actuator/health" "" "" \
        "200" "Health check"
    
    echo "========================================"
    print_success "All tests completed!"
    echo "========================================"
}

# Performance test function
run_performance_tests() {
    echo "========================================"
    echo "Performance Tests"
    echo "========================================"
    
    local admin_token=$(get_auth_token $ADMIN_USER $ADMIN_PASS)
    
    print_status "Running performance tests..."
    
    # Test concurrent requests
    print_status "Testing concurrent traffic data ingestion..."
    
    for i in {1..10}; do
        local traffic_data='{
            "location": "Perf Test Location '$i'",
            "latitude": '$(echo "16.5062 + $i * 0.001" | bc)',
            "longitude": '$(echo "80.6480 + $i * 0.001" | bc)',
            "trafficDensity": "MODERATE",
            "timestamp": "'$(date -u +"%Y-%m-%dT%H:%M:%S")'"
        }'
        
        curl -s -X POST "$API_BASE_URL/traffic/ingest" \
            -H "Authorization: Bearer $admin_token" \
            -H "Content-Type: application/json" \
            -d "$traffic_data" &
    done
    
    wait
    print_success "Concurrent ingestion test completed"
    
    # Test prediction performance
    print_status "Testing prediction performance..."
    
    local start_time=$(date +%s%N)
    for i in {1..20}; do
        curl -s "$API_BASE_URL/traffic/predict?lat=16.5062&lon=80.6480&timestamp=$(date -u +"%Y-%m-%dT%H:%M:%S")" \
            -H "Authorization: Bearer $admin_token" > /dev/null
    done
    local end_time=$(date +%s%N)
    
    local duration=$(( (end_time - start_time) / 1000000 ))
    print_success "20 predictions completed in ${duration}ms (avg: $((duration/20))ms per request)"
}

# Check dependencies
check_dependencies() {
    local missing_deps=()
    
    if ! command -v curl &> /dev/null; then
        missing_deps+=("curl")
    fi
    
    if ! command -v jq &> /dev/null; then
        missing_deps+=("jq")
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        print_error "Missing dependencies: ${missing_deps[*]}"
        print_warning "Please install missing dependencies and try again"
        exit 1
    fi
}

# Main script
main() {
    check_dependencies
    
    case "${1:-test}" in
        "test")
            run_tests
            ;;
        "perf")
            run_performance_tests
            ;;
        "all")
            run_tests
            run_performance_tests
            ;;
        *)
            echo "Usage: $0 [test|perf|all]"
            echo "  test - Run API tests (default)"
            echo "  perf - Run performance tests"
            echo "  all  - Run both API and performance tests"
            exit 1
            ;;
    esac
}

main "$@"
