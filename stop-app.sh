#!/bin/bash

# Smart Traffic Management System - Stop Script
# This script stops both the backend API and frontend web server

echo "🛑 Stopping Smart Traffic Management System..."
echo "============================================="

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

# Function to kill process on port
kill_port() {
    local port=$1
    local service=$2
    
    print_info "Stopping $service on port $port..."
    
    # Find process using the port
    PID=$(lsof -ti:$port)
    
    if [ ! -z "$PID" ]; then
        # Try graceful shutdown first
        kill $PID 2>/dev/null
        sleep 3
        
        # Check if process is still running
        if kill -0 $PID 2>/dev/null; then
            print_warning "Graceful shutdown failed, forcing termination..."
            kill -9 $PID 2>/dev/null
            sleep 2
        fi
        
        # Verify process is stopped
        if ! kill -0 $PID 2>/dev/null; then
            print_status "$service stopped successfully"
        else
            print_error "Failed to stop $service"
        fi
    else
        print_warning "$service was not running on port $port"
    fi
}

# Stop backend API (port 8081)
kill_port 8081 "Backend API"

# Stop frontend server (port 3000)
kill_port 3000 "Frontend Server"

# Stop any Maven processes
print_info "Stopping Maven processes..."
pkill -f "mvn spring-boot:run" 2>/dev/null
if [ $? -eq 0 ]; then
    print_status "Maven processes stopped"
else
    print_warning "No Maven processes found"
fi

# Stop any Python HTTP server processes
print_info "Stopping Python HTTP server processes..."
pkill -f "python3 -m http.server 3000" 2>/dev/null
if [ $? -eq 0 ]; then
    print_status "Python HTTP server stopped"
else
    print_warning "No Python HTTP server found"
fi

# Clean up PID files
if [ -f "backend.pid" ]; then
    rm backend.pid
    print_info "Removed backend.pid file"
fi

if [ -f "frontend.pid" ]; then
    rm frontend.pid
    print_info "Removed frontend.pid file"
fi

# Clean up log files (optional)
read -p "Would you like to clean up log files? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    if [ -f "backend.log" ]; then
        rm backend.log
        print_info "Removed backend.log"
    fi
    
    if [ -f "frontend.log" ]; then
        rm frontend.log
        print_info "Removed frontend.log"
    fi
    
    print_status "Log files cleaned up"
fi

# Final verification
print_info "Verifying all services are stopped..."

# Check ports
BACKEND_CHECK=$(lsof -ti:8081)
FRONTEND_CHECK=$(lsof -ti:3000)

if [ -z "$BACKEND_CHECK" ] && [ -z "$FRONTEND_CHECK" ]; then
    echo ""
    print_status "All services stopped successfully!"
    echo ""
    echo "📊 Summary:"
    echo "   ✅ Backend API (port 8081) - Stopped"
    echo "   ✅ Frontend Server (port 3000) - Stopped"
    echo "   ✅ All processes terminated"
    echo ""
    echo "🚀 To restart the application:"
    echo "   Run: ./start-app.sh"
    echo ""
else
    echo ""
    print_warning "Some services may still be running:"
    if [ ! -z "$BACKEND_CHECK" ]; then
        echo "   ⚠️  Backend API still running on port 8081 (PID: $BACKEND_CHECK)"
    fi
    if [ ! -z "$FRONTEND_CHECK" ]; then
        echo "   ⚠️  Frontend Server still running on port 3000 (PID: $FRONTEND_CHECK)"
    fi
    echo ""
    echo "🔧 Manual cleanup may be required:"
    echo "   sudo kill -9 $BACKEND_CHECK $FRONTEND_CHECK"
fi

echo "🛑 Shutdown process complete!"
