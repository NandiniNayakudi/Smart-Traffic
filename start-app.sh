#!/bin/bash

# Smart Traffic Management System - Startup Script
# This script starts both the backend API and frontend web server

echo "🚦 Starting Smart Traffic Management System..."
echo "=============================================="

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ️${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠️${NC} $1"
}

print_error() {
    echo -e "${RED}❌${NC} $1"
}

# Check if Java is installed
if ! command -v java &> /dev/null; then
    print_error "Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    print_error "Maven is not installed. Please install Maven."
    exit 1
fi

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    print_error "Python 3 is not installed. Please install Python 3."
    exit 1
fi

print_info "All prerequisites are installed."

# Function to check if port is in use
check_port() {
    if lsof -Pi :$1 -sTCP:LISTEN -t >/dev/null ; then
        return 0
    else
        return 1
    fi
}

# Check if backend is already running
if check_port 8081; then
    print_warning "Backend API is already running on port 8081"
    BACKEND_RUNNING=true
else
    BACKEND_RUNNING=false
fi

# Check if frontend is already running
if check_port 3000; then
    print_warning "Frontend server is already running on port 3000"
    FRONTEND_RUNNING=true
else
    FRONTEND_RUNNING=false
fi

# Start backend if not running
if [ "$BACKEND_RUNNING" = false ]; then
    print_info "Starting backend API server..."
    
    # Build the project first
    print_info "Building the project..."
    mvn clean compile -q
    
    if [ $? -eq 0 ]; then
        print_status "Project built successfully"
        
        # Start the Spring Boot application in background
        print_info "Starting Spring Boot application..."
        nohup mvn spring-boot:run > backend.log 2>&1 &
        BACKEND_PID=$!
        
        # Wait for backend to start
        print_info "Waiting for backend to start..."
        for i in {1..30}; do
            if check_port 8081; then
                print_status "Backend API started successfully on port 8081"
                break
            fi
            sleep 2
            echo -n "."
        done
        
        if ! check_port 8081; then
            print_error "Backend failed to start. Check backend.log for details."
            exit 1
        fi
    else
        print_error "Failed to build the project"
        exit 1
    fi
else
    print_status "Backend API is already running"
fi

# Start frontend if not running
if [ "$FRONTEND_RUNNING" = false ]; then
    print_info "Starting frontend web server..."
    
    # Check if frontend directory exists
    if [ ! -d "frontend" ]; then
        print_error "Frontend directory not found"
        exit 1
    fi
    
    # Start Python HTTP server for frontend
    cd frontend
    nohup python3 -m http.server 3000 > ../frontend.log 2>&1 &
    FRONTEND_PID=$!
    cd ..
    
    # Wait for frontend to start
    print_info "Waiting for frontend to start..."
    for i in {1..10}; do
        if check_port 3000; then
            print_status "Frontend server started successfully on port 3000"
            break
        fi
        sleep 1
        echo -n "."
    done
    
    if ! check_port 3000; then
        print_error "Frontend failed to start. Check frontend.log for details."
        exit 1
    fi
else
    print_status "Frontend server is already running"
fi

# Test the application
print_info "Testing the application..."

# Test backend API
BACKEND_TEST=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/v1/auth/login -X POST -H "Content-Type: application/json" -d '{"username":"admin","password":"secure123"}')

if [ "$BACKEND_TEST" = "200" ]; then
    print_status "Backend API is responding correctly"
else
    print_warning "Backend API test returned status: $BACKEND_TEST"
fi

# Test frontend
FRONTEND_TEST=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000)

if [ "$FRONTEND_TEST" = "200" ]; then
    print_status "Frontend server is responding correctly"
else
    print_warning "Frontend server test returned status: $FRONTEND_TEST"
fi

echo ""
echo "🎉 Smart Traffic Management System is now running!"
echo "=================================================="
echo ""
echo "📱 Frontend Application:"
echo "   URL: http://localhost:3000"
echo "   Landing Page: http://localhost:3000/index.html"
echo "   Login Page: http://localhost:3000/pages/login.html"
echo ""
echo "🔧 Backend API:"
echo "   Base URL: http://localhost:8081/api/v1"
echo "   H2 Console: http://localhost:8081/api/v1/h2-console"
echo ""
echo "🔐 Demo Credentials:"
echo "   Administrator: admin / secure123"
echo "   Traffic Manager: traffic_manager / traffic2024"
echo "   Regular User: user / password123"
echo ""
echo "📊 Features Available:"
echo "   ✅ User Authentication & Authorization"
echo "   ✅ Real-time Traffic Dashboard"
echo "   ✅ Traffic Monitoring & Analytics"
echo "   ✅ Signal Optimization"
echo "   ✅ Route Planning"
echo "   ✅ Trend Analysis"
echo "   ✅ Responsive Design (Mobile/Desktop)"
echo ""
echo "📝 Logs:"
echo "   Backend: backend.log"
echo "   Frontend: frontend.log"
echo ""
echo "🛑 To stop the application:"
echo "   Run: ./stop-app.sh"
echo "   Or manually kill processes on ports 8081 and 3000"
echo ""
echo "🌐 Open your web browser and navigate to:"
echo "   http://localhost:3000"
echo ""
print_status "Application startup complete!"

# Save process IDs for stopping later
if [ ! -z "$BACKEND_PID" ]; then
    echo $BACKEND_PID > backend.pid
fi

if [ ! -z "$FRONTEND_PID" ]; then
    echo $FRONTEND_PID > frontend.pid
fi

# Keep script running to show real-time status
echo ""
print_info "Press Ctrl+C to view logs or use './stop-app.sh' to stop the application"
echo ""

# Optional: Show live logs
read -p "Would you like to view live backend logs? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_info "Showing live backend logs (Press Ctrl+C to exit):"
    tail -f backend.log
fi
