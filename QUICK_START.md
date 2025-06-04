# 🚀 Quick Start Guide - Traffic Management System

## ⚡ 5-Minute Setup

### Prerequisites
- Java 17+
- Maven 3.6+
- (Optional) Docker & Docker Compose

### Option 1: Local Development (Recommended for testing)

1. **Clone and Start**:
   ```bash
   # Windows
   scripts\start-system.bat
   
   # Linux/Mac
   mvn spring-boot:run
   ```

2. **Access the System**:
   - **API Base**: http://localhost:8080/api/v1
   - **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
   - **H2 Console**: http://localhost:8080/api/v1/h2-console

3. **Test the APIs**:
   ```bash
   # Windows
   scripts\run-tests.bat
   
   # Linux/Mac
   ./scripts/api-test.sh
   ```

### Option 2: Docker (Production-like)

1. **Start All Services**:
   ```bash
   docker-compose up -d
   ```

2. **Access Services**:
   - **API**: http://localhost/api/v1
   - **Grafana**: http://localhost:3000 (admin/admin123)
   - **Prometheus**: http://localhost:9090

## 🔐 Demo Credentials

| Username | Password | Role | Access |
|----------|----------|------|--------|
| admin | secure123 | ADMIN | Full access |
| traffic_manager | traffic2024 | TRAFFIC_MANAGER | Traffic operations |
| user | password123 | USER | Read-only |

## 📊 Quick API Test

### 1. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"secure123"}'
```

### 2. Ingest Traffic Data
```bash
curl -X POST http://localhost:8080/api/v1/traffic/ingest \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "location": "Vijayawada Junction",
    "latitude": 16.5062,
    "longitude": 80.6480,
    "trafficDensity": "HIGH"
  }'
```

### 3. Get Traffic Prediction
```bash
curl "http://localhost:8080/api/v1/traffic/predict?lat=16.5062&lon=80.6480&timestamp=2024-01-15T12:00:00" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 4. Get Route Recommendation
```bash
curl "http://localhost:8080/api/v1/traffic/route?source=Vijayawada%20Junction&destination=PNBS%20Bus%20Stand&eco=true" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🎯 Sample Data Generation

```bash
# Install Python dependencies
pip install requests

# Generate sample data
python scripts/sample-data-generator.py
```

## 🔧 Troubleshooting

### Common Issues

1. **Port 8080 already in use**:
   ```bash
   # Change port in application.yml
   server.port: 8081
   ```

2. **Java version issues**:
   ```bash
   java -version  # Should be 17+
   ```

3. **Maven build fails**:
   ```bash
   mvn clean install -DskipTests
   ```

4. **Docker issues**:
   ```bash
   docker-compose down
   docker system prune -f
   docker-compose up -d
   ```

### Health Checks

- **Application**: http://localhost:8080/api/v1/actuator/health
- **Database**: Check H2 console or MySQL connection
- **Authentication**: Try login endpoint

## 📈 Next Steps

1. **Explore Swagger UI** for interactive API testing
2. **Generate sample data** using the Python script
3. **Set up monitoring** with Grafana dashboards
4. **Configure Google Maps API** for enhanced routing
5. **Deploy to production** using Docker Compose

## 🆘 Need Help?

- **Documentation**: Check README.md for detailed information
- **API Reference**: Use Swagger UI for interactive docs
- **Sample Data**: Run the data generator script
- **Tests**: Execute the test scripts to verify functionality

---

**🎉 You're ready to go! The Traffic Management System is now running and ready for testing.**
