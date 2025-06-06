package com.traffic.service;

import com.traffic.model.TrafficData;
import com.traffic.dto.TrafficAlert;
import com.traffic.controller.RealTimeTrafficController;
import com.traffic.repository.TrafficDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing real-time traffic data streaming and analytics
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RealTimeTrafficService {

    private final TrafficDataRepository trafficDataRepository;
    private final TrafficIngestionService trafficIngestionService;
    private RealTimeTrafficController realTimeController;
    
    // Cache for real-time data
    private final Map<String, TrafficData> currentTrafficCache = new ConcurrentHashMap<>();
    private final List<TrafficAlert> activeAlerts = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Object> currentAnalytics = new ConcurrentHashMap<>();

    /**
     * Set the real-time controller (to avoid circular dependency)
     */
    public void setRealTimeController(RealTimeTrafficController controller) {
        this.realTimeController = controller;
    }

    /**
     * Process new traffic data and broadcast updates
     */
    @Async
    public void processRealTimeTrafficData(TrafficData trafficData) {
        try {
            // Update cache
            currentTrafficCache.put(trafficData.getLocation(), trafficData);
            
            // Check for alerts
            checkForTrafficAlerts(trafficData);
            
            // Update analytics
            updateRealTimeAnalytics();
            
            // Broadcast to WebSocket subscribers
            if (realTimeController != null) {
                realTimeController.broadcastTrafficUpdate(trafficData);
            }
            
            log.debug("Processed real-time traffic data for: {}", trafficData.getLocation());
            
        } catch (Exception e) {
            log.error("Error processing real-time traffic data", e);
        }
    }

    /**
     * Get current traffic snapshot for new subscribers
     */
    public Map<String, Object> getCurrentTrafficSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("type", "TRAFFIC_SNAPSHOT");
        snapshot.put("timestamp", System.currentTimeMillis());
        snapshot.put("locations", new ArrayList<>(currentTrafficCache.values()));
        snapshot.put("totalLocations", currentTrafficCache.size());
        
        return snapshot;
    }

    /**
     * Get current alerts
     */
    public Map<String, Object> getCurrentAlerts() {
        Map<String, Object> alerts = new HashMap<>();
        alerts.put("type", "ALERTS_SNAPSHOT");
        alerts.put("timestamp", System.currentTimeMillis());
        alerts.put("alerts", new ArrayList<>(activeAlerts));
        alerts.put("totalAlerts", activeAlerts.size());
        
        return alerts;
    }

    /**
     * Get current analytics
     */
    public Map<String, Object> getCurrentAnalytics() {
        Map<String, Object> analytics = new HashMap<>(currentAnalytics);
        analytics.put("type", "ANALYTICS_SNAPSHOT");
        analytics.put("timestamp", System.currentTimeMillis());
        
        return analytics;
    }

    /**
     * Get traffic data for specific location
     */
    public Map<String, Object> getLocationTraffic(String locationId) {
        String location = locationId.replaceAll("_", " ");
        TrafficData data = currentTrafficCache.values().stream()
            .filter(td -> td.getLocation().toLowerCase().contains(location.toLowerCase()))
            .findFirst()
            .orElse(null);
            
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("type", "LOCATION_TRAFFIC");
        locationData.put("locationId", locationId);
        locationData.put("timestamp", System.currentTimeMillis());
        locationData.put("data", data);
        
        return locationData;
    }

    /**
     * Check for traffic alerts based on new data
     */
    private void checkForTrafficAlerts(TrafficData trafficData) {
        List<TrafficAlert> newAlerts = new ArrayList<>();
        
        // High traffic density alert
        if (trafficData.getTrafficDensity() == TrafficData.TrafficDensity.HIGH) {
            TrafficAlert alert = new TrafficAlert();
            alert.setId(UUID.randomUUID().toString());
            alert.setType("HIGH_TRAFFIC");
            alert.setSeverity("WARNING");
            alert.setLocation(trafficData.getLocation());
            alert.setMessage("High traffic density detected at " + trafficData.getLocation());
            alert.setTimestamp(LocalDateTime.now());
            alert.setActive(true);
            
            newAlerts.add(alert);
        }
        
        // Low speed alert
        if (trafficData.getAverageSpeed() != null && trafficData.getAverageSpeed() < 15.0) {
            TrafficAlert alert = new TrafficAlert();
            alert.setId(UUID.randomUUID().toString());
            alert.setType("LOW_SPEED");
            alert.setSeverity("CRITICAL");
            alert.setLocation(trafficData.getLocation());
            alert.setMessage("Very low average speed (" + trafficData.getAverageSpeed() + " km/h) at " + trafficData.getLocation());
            alert.setTimestamp(LocalDateTime.now());
            alert.setActive(true);
            
            newAlerts.add(alert);
        }
        
        // Add new alerts and broadcast
        for (TrafficAlert alert : newAlerts) {
            activeAlerts.add(alert);
            if (realTimeController != null) {
                realTimeController.broadcastAlert(alert);
            }
        }
        
        // Clean up old alerts (older than 1 hour)
        activeAlerts.removeIf(alert -> 
            alert.getTimestamp().isBefore(LocalDateTime.now().minusHours(1))
        );
    }

    /**
     * Update real-time analytics
     */
    private void updateRealTimeAnalytics() {
        if (currentTrafficCache.isEmpty()) return;
        
        Collection<TrafficData> allData = currentTrafficCache.values();
        
        // Calculate statistics
        long highTrafficCount = allData.stream()
            .mapToLong(td -> td.getTrafficDensity() == TrafficData.TrafficDensity.HIGH ? 1 : 0)
            .sum();
            
        double avgSpeed = allData.stream()
            .filter(td -> td.getAverageSpeed() != null)
            .mapToDouble(TrafficData::getAverageSpeed)
            .average()
            .orElse(0.0);
            
        int totalVehicles = allData.stream()
            .filter(td -> td.getVehicleCount() != null)
            .mapToInt(TrafficData::getVehicleCount)
            .sum();
        
        // Update analytics cache
        currentAnalytics.put("totalLocations", allData.size());
        currentAnalytics.put("highTrafficLocations", highTrafficCount);
        currentAnalytics.put("averageSpeed", Math.round(avgSpeed * 10.0) / 10.0);
        currentAnalytics.put("totalVehicles", totalVehicles);
        currentAnalytics.put("activeAlerts", activeAlerts.size());
        currentAnalytics.put("lastUpdate", System.currentTimeMillis());
        
        // Broadcast analytics update
        if (realTimeController != null) {
            realTimeController.broadcastAnalyticsUpdate(currentAnalytics);
        }
    }

    /**
     * Scheduled task to generate simulated real-time data for demonstration
     */
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void generateSimulatedRealTimeData() {
        try {
            List<TrafficData> simulatedData = trafficIngestionService.generateSampleTrafficData();
            
            // Process a random subset of the data to simulate real-time updates
            simulatedData.stream()
                .limit(3) // Process 3 random locations
                .forEach(this::processRealTimeTrafficData);
                
        } catch (Exception e) {
            log.error("Error generating simulated real-time data", e);
        }
    }

    /**
     * Scheduled task to clean up old data
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupOldData() {
        // Remove traffic data older than 30 minutes from cache
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        currentTrafficCache.entrySet().removeIf(entry -> 
            entry.getValue().getTimestamp().isBefore(cutoff)
        );
        
        log.debug("Cleaned up old traffic data. Current cache size: {}", currentTrafficCache.size());
    }
}
