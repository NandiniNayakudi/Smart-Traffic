package com.traffic.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Mock Google Cloud Service for Development
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */
@Service
@Slf4j
public class MockGoogleCloudService {

    /**
     * Get mock analytics summary
     */
    public Map<String, Object> getCurrentTrafficSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalLocations", 10L);
        summary.put("averageSpeed", 35.5 + (Math.random() * 10 - 5)); // 30.5-40.5 km/h
        summary.put("totalVehicles", 450L + (long)(Math.random() * 100 - 50)); // 400-500 vehicles
        summary.put("highTrafficCount", (long)(Math.random() * 5)); // 0-4
        summary.put("criticalTrafficCount", (long)(Math.random() * 2)); // 0-1
        summary.put("timestamp", LocalDateTime.now());
        summary.put("source", "MOCK_DATA");
        
        log.debug("Generated mock traffic summary: {}", summary);
        return summary;
    }

    /**
     * Get mock congestion hotspots
     */
    public List<Map<String, Object>> getCongestionHotspots() {
        List<Map<String, Object>> hotspots = new ArrayList<>();
        
        String[] locations = {
            "Times Square, New York",
            "Union Square, New York", 
            "Brooklyn Bridge, New York",
            "Manhattan Bridge, New York"
        };
        
        double[][] coordinates = {
            {40.7580, -73.9855},
            {40.7359, -73.9911},
            {40.7061, -73.9969},
            {40.7071, -73.9903}
        };
        
        for (int i = 0; i < locations.length; i++) {
            if (Math.random() > 0.3) { // 70% chance of being a hotspot
                Map<String, Object> hotspot = new HashMap<>();
                hotspot.put("location", locations[i]);
                hotspot.put("latitude", coordinates[i][0]);
                hotspot.put("longitude", coordinates[i][1]);
                hotspot.put("averageSpeed", 15.0 + (Math.random() * 20)); // 15-35 km/h
                hotspot.put("congestionCount", 5L + (long)(Math.random() * 10)); // 5-15
                hotspot.put("source", "MOCK_DATA");
                hotspots.add(hotspot);
            }
        }
        
        log.debug("Generated {} mock congestion hotspots", hotspots.size());
        return hotspots;
    }

    /**
     * Get mock traffic trends
     */
    public List<Map<String, Object>> getTrafficTrends() {
        List<Map<String, Object>> trends = new ArrayList<>();
        
        for (int i = 0; i < 12; i++) { // Last 12 time periods
            Map<String, Object> trend = new HashMap<>();
            LocalDateTime time = LocalDateTime.now().minusMinutes(i * 10);
            
            trend.put("timestamp", time.toString());
            trend.put("averageSpeed", 25.0 + (Math.random() * 20)); // 25-45 km/h
            trend.put("dataPoints", 10L + (long)(Math.random() * 15)); // 10-25
            trend.put("highTrafficCount", (long)(Math.random() * 8)); // 0-7
            trend.put("source", "MOCK_DATA");
            trends.add(trend);
        }
        
        // Sort by timestamp descending
        trends.sort((a, b) -> b.get("timestamp").toString().compareTo(a.get("timestamp").toString()));
        
        log.debug("Generated {} mock traffic trends", trends.size());
        return trends;
    }

    /**
     * Get mock average speeds
     */
    public Map<String, Double> getAverageSpeeds() {
        Map<String, Double> speeds = new HashMap<>();
        
        speeds.put("Times Square, New York", 15.2 + (Math.random() * 10));
        speeds.put("Union Square, New York", 25.8 + (Math.random() * 10));
        speeds.put("Brooklyn Bridge, New York", 30.5 + (Math.random() * 10));
        speeds.put("Manhattan Bridge, New York", 28.3 + (Math.random() * 10));
        speeds.put("Lincoln Tunnel, New York", 22.1 + (Math.random() * 10));
        
        log.debug("Generated mock average speeds for {} locations", speeds.size());
        return speeds;
    }

    /**
     * Get mock route traffic conditions
     */
    public Map<String, Object> getRouteTrafficConditions(String origin, String destination) {
        Map<String, Object> conditions = new HashMap<>();
        
        // Generate realistic mock data
        double distance = 3.0 + (Math.random() * 10); // 3-13 km
        double baseTime = distance * 2; // 2 minutes per km base
        double trafficFactor = 1.2 + (Math.random() * 0.8); // 1.2-2.0x
        
        conditions.put("origin", origin);
        conditions.put("destination", destination);
        conditions.put("distance", String.format("%.1f km", distance));
        conditions.put("distanceMeters", (int)(distance * 1000));
        conditions.put("duration", String.format("%.0f mins", baseTime));
        conditions.put("durationSeconds", (long)(baseTime * 60));
        conditions.put("durationInTraffic", String.format("%.0f mins", baseTime * trafficFactor));
        conditions.put("durationInTrafficSeconds", (long)(baseTime * trafficFactor * 60));
        conditions.put("trafficDelay", (long)((trafficFactor - 1) * baseTime * 60));
        conditions.put("trafficDelayMinutes", (trafficFactor - 1) * baseTime);
        
        String trafficLevel;
        if (trafficFactor > 1.8) trafficLevel = "HEAVY";
        else if (trafficFactor > 1.5) trafficLevel = "MODERATE";
        else if (trafficFactor > 1.2) trafficLevel = "LIGHT";
        else trafficLevel = "FREE_FLOW";
        
        conditions.put("trafficLevel", trafficLevel);
        conditions.put("timestamp", LocalDateTime.now());
        conditions.put("source", "MOCK_DATA");
        
        log.debug("Generated mock route conditions from {} to {}: {}", origin, destination, trafficLevel);
        return conditions;
    }

    /**
     * Mock traffic data fetch trigger
     */
    public Map<String, Object> triggerTrafficFetch() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Mock traffic data fetch triggered");
        response.put("timestamp", System.currentTimeMillis());
        response.put("source", "MOCK_SERVICE");
        
        log.info("Mock traffic data fetch triggered");
        return response;
    }

    /**
     * Get mock connectivity status
     */
    public Map<String, Object> getConnectivityStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("googleMaps", false);
        status.put("bigQuery", false);
        status.put("pubSub", false);
        status.put("mode", "MOCK");
        status.put("message", "Running in demo mode with mock data");
        
        return status;
    }

    /**
     * Get mock Pub/Sub status
     */
    public Map<String, Object> getPubSubStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("publisherAvailable", false);
        status.put("subscriberRunning", false);
        status.put("projectId", "mock-project");
        status.put("topic", "mock-topic");
        status.put("mode", "MOCK");
        
        return status;
    }

    /**
     * Mock real-time traffic data generation
     */
    public void generateMockRealTimeData() {
        // This would be called periodically to simulate real-time updates
        log.debug("Generating mock real-time traffic data");
        
        // Could trigger WebSocket updates with mock data
        // This is handled by the existing RealTimeTrafficService
    }

    /**
     * Check if Google Cloud services are available
     */
    public boolean isGoogleCloudAvailable() {
        return false; // Always false for mock service
    }

    /**
     * Get service status
     */
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("serviceName", "MockGoogleCloudService");
        status.put("status", "ACTIVE");
        status.put("mode", "DEVELOPMENT");
        status.put("description", "Providing mock data for development and demonstration");
        status.put("capabilities", Arrays.asList(
            "Mock traffic analytics",
            "Mock congestion detection", 
            "Mock route optimization",
            "Mock real-time updates"
        ));
        status.put("timestamp", LocalDateTime.now());
        
        return status;
    }
}
