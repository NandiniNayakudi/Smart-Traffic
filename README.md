# 🚦 Traffic Management System

A comprehensive REST API system for smart traffic management with ML-based predictions, route optimization, and signal control.

## 🌟 Features

- **Real-time Traffic Data Ingestion** - Accept and process live traffic data
- **ML-based Traffic Prediction** - Predict congestion using machine learning
- **Route Optimization** - Eco-friendly route recommendations
- **Traffic Signal Optimization** - Dynamic signal timing optimization
- **Historical Trend Analysis** - Traffic pattern analysis for city planners
- **JWT Authentication** - Secure API access
- **Comprehensive Documentation** - Swagger/OpenAPI integration

## 🏗️ Architecture

### Base URL
```
/api/v1/traffic
```

### Core Modules
- `TrafficController` - Main traffic management endpoints
- `AuthController` - Authentication and authorization
- `PredictionService` - ML model integration
- `RouteService` - Route optimization logic
- `SignalOptimizationService` - Traffic signal management
- `TrendAnalysisService` - Historical data analysis

## 🔒 Security Notice

**⚠️ IMPORTANT:** This repository contains template configuration files with placeholder values. Before deploying:

1. **Never commit real credentials** to version control
2. **Update all placeholder values** with your actual Google Cloud project details
3. **Use environment variables** for sensitive configuration
4. **Review CONFIGURATION_TEMPLATE.md** for secure setup instructions

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- (Optional) Google Maps API key for enhanced routing

### 1. Clone and Build
```bash
git clone <repository-url>
cd traffic-management-system
mvn clean install
```

### 2. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api/v1`

### 3. Access Documentation
- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **H2 Database Console**: http://localhost:8080/api/v1/h2-console

## 🔐 Authentication

### Login
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "secure123"
}
```

### Demo Users
- **admin** / secure123 (Full access)
- **traffic_manager** / traffic2024 (Traffic management)
- **user** / password123 (Read access)

### Using JWT Token
```bash
Authorization: Bearer <your-jwt-token>
```

## 📊 API Endpoints

### 1. Traffic Data Ingestion
```bash
POST /api/v1/traffic/ingest
{
  "location": "MG Road, Vijayawada",
  "latitude": 16.5062,
  "longitude": 80.6480,
  "trafficDensity": "HIGH",
  "timestamp": "2025-06-04T11:30:00Z"
}
```

### 2. Traffic Prediction
```bash
GET /api/v1/traffic/predict?lat=16.5062&lon=80.6480&timestamp=2025-06-04T12:00:00Z
```

### 3. Route Recommendation
```bash
GET /api/v1/traffic/route?source=Vijayawada+Junction&destination=PNBS+Bus+Stand&eco=true
```

### 4. Signal Optimization
```bash
POST /api/v1/traffic/signal/optimize
{
  "intersectionId": "INT-112",
  "north": 50,
  "south": 30,
  "east": 10,
  "west": 20
}
```

### 5. Traffic Trends
```bash
GET /api/v1/traffic/trends?location=Vijayawada&period=monthly
```

## 🛠️ Configuration

### Environment Variables
```bash
# Google Maps API (Optional)
GOOGLE_MAPS_API_KEY=your-google-maps-api-key

# ML Model Endpoint (Optional)
ML_MODEL_ENDPOINT=http://your-ml-service:5000/predict
```

### Database Configuration
The application uses H2 in-memory database by default. For production, configure MySQL:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/traffic_db
    username: your_username
    password: your_password
```

## 🧪 Testing

### Run Unit Tests
```bash
mvn test
```

### Run Integration Tests
```bash
mvn test -Dtest=**/*IntegrationTest
```

### Automated API Testing

#### Windows:
```bash
scripts\run-tests.bat
```

#### Linux/Mac:
```bash
chmod +x scripts/api-test.sh
./scripts/api-test.sh
```

### Generate Sample Data
```bash
# Install Python dependencies
pip install requests

# Run sample data generator
python scripts/sample-data-generator.py
```

### Manual Testing with cURL

1. **Login**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"secure123"}'
```

2. **Ingest Traffic Data**:
```bash
curl -X POST http://localhost:8080/api/v1/traffic/ingest \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "location": "Test Location",
    "latitude": 16.5062,
    "longitude": 80.6480,
    "trafficDensity": "MODERATE"
  }'
```

### Test Coverage
- **Unit Tests**: 95%+ coverage for service layer
- **Integration Tests**: Complete API workflow testing
- **Performance Tests**: Concurrent request handling
- **Security Tests**: Authentication and authorization

## 📈 Monitoring

### Health Check
```bash
GET /api/v1/actuator/health
```

### Metrics
```bash
GET /api/v1/actuator/metrics
```

## 🔧 Development

### Project Structure
```
src/
├── main/java/com/traffic/
│   ├── controller/          # REST controllers
│   ├── service/            # Business logic
│   ├── model/              # JPA entities
│   ├── dto/                # Data transfer objects
│   ├── repository/         # Data access layer
│   ├── config/             # Configuration classes
│   ├── security/           # Security components
│   └── exception/          # Exception handling
└── test/                   # Test classes
```

### Adding New Features
1. Create DTOs in `dto/` package
2. Add business logic in `service/` package
3. Create REST endpoints in `controller/` package
4. Add tests in `test/` package

## 🚀 Deployment

### Quick Start (Windows)
```bash
# Start the system
scripts\start-system.bat

# Run tests
scripts\run-tests.bat
```

### Docker Deployment
```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f traffic-app

# Stop services
docker-compose down
```

### Docker Services Included
- **Traffic Management App** - Main Spring Boot application
- **MySQL Database** - Production database with sample data
- **Redis Cache** - For caching and session management
- **Nginx** - Reverse proxy with rate limiting
- **ML Service** - Mock machine learning service
- **Prometheus** - Metrics collection
- **Grafana** - Monitoring dashboards

### Environment Variables
```bash
# Required for production
GOOGLE_MAPS_API_KEY=your-google-maps-api-key
ML_MODEL_ENDPOINT=http://your-ml-service:5000/predict
JWT_SECRET=your-secure-jwt-secret-key

# Database (if using external)
SPRING_DATASOURCE_URL=jdbc:mysql://your-db:3306/traffic_db
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password
```

### Production Considerations
- Use external database (MySQL/PostgreSQL)
- Configure proper JWT secrets
- Set up monitoring and logging
- Implement rate limiting (included in Nginx config)
- Add API versioning strategy
- Enable HTTPS with SSL certificates
- Set up backup and disaster recovery

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 📞 Support

For questions or support, please contact the development team or create an issue in the repository.

---

**Built with ❤️ for Smart City Traffic Management**
