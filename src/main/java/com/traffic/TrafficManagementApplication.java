package com.traffic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Traffic Management System
 * 
 * Features:
 * - Real-time traffic data ingestion
 * - ML-based traffic prediction
 * - Route optimization
 * - Traffic signal optimization
 * - Historical trend analysis
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class TrafficManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrafficManagementApplication.class, args);
        System.out.println("🚦 Traffic Management System Started Successfully!");
        System.out.println("📊 API Documentation: http://localhost:8080/api/v1/swagger-ui.html");
        System.out.println("🗄️  H2 Console: http://localhost:8080/api/v1/h2-console");
    }
}
