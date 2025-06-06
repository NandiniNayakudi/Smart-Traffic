package com.traffic.controller;

import com.traffic.model.TrafficData;
import com.traffic.dto.TrafficAlert;
import com.traffic.service.RealTimeTrafficService;
import com.traffic.service.RealTimePerformanceMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Real-time WebSocket controller for live traffic data streaming
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class RealTimeTrafficController {

    private final RealTimeTrafficService realTimeTrafficService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RealTimePerformanceMonitor performanceMonitor;

    /**
     * Handle subscription to real-time traffic updates
     */
    @MessageMapping("/traffic/subscribe")
    @SendTo("/topic/traffic")
    public Map<String, Object> subscribeToTrafficUpdates() {
        log.info("Client subscribed to real-time traffic updates");
        return realTimeTrafficService.getCurrentTrafficSnapshot();
    }

    /**
     * Handle subscription to traffic alerts
     */
    @MessageMapping("/alerts/subscribe")
    @SendTo("/topic/alerts")
    public Map<String, Object> subscribeToAlerts() {
        log.info("Client subscribed to real-time alerts");
        return realTimeTrafficService.getCurrentAlerts();
    }

    /**
     * Handle subscription to analytics updates
     */
    @MessageMapping("/analytics/subscribe")
    @SendTo("/topic/analytics")
    public Map<String, Object> subscribeToAnalytics() {
        log.info("Client subscribed to real-time analytics");
        return realTimeTrafficService.getCurrentAnalytics();
    }

    /**
     * Handle subscription to performance monitoring
     */
    @MessageMapping("/performance/subscribe")
    @SendTo("/topic/performance")
    public Map<String, Object> subscribeToPerformance() {
        log.info("Client subscribed to performance monitoring");
        return performanceMonitor.getCurrentMetrics();
    }

    /**
     * Handle location-specific traffic subscription
     */
    @MessageMapping("/traffic/location/{locationId}")
    public void subscribeToLocationTraffic(@PathVariable String locationId) {
        log.info("Client subscribed to location-specific traffic: {}", locationId);
        Map<String, Object> locationData = realTimeTrafficService.getLocationTraffic(locationId);
        messagingTemplate.convertAndSend("/topic/traffic/location/" + locationId, locationData);
    }

    /**
     * Broadcast new traffic data to all subscribers
     */
    public void broadcastTrafficUpdate(TrafficData trafficData) {
        log.debug("Broadcasting traffic update for location: {}", trafficData.getLocation());
        
        Map<String, Object> update = Map.of(
            "type", "TRAFFIC_UPDATE",
            "timestamp", System.currentTimeMillis(),
            "data", trafficData
        );
        
        messagingTemplate.convertAndSend("/topic/traffic", update);
        
        // Also send to location-specific subscribers
        String locationId = trafficData.getLocation().replaceAll("\\s+", "_").toLowerCase();
        messagingTemplate.convertAndSend("/topic/traffic/location/" + locationId, update);
    }

    /**
     * Broadcast traffic alert to all subscribers
     */
    public void broadcastAlert(TrafficAlert alert) {
        log.info("Broadcasting traffic alert: {}", alert.getMessage());
        
        Map<String, Object> alertUpdate = Map.of(
            "type", "ALERT",
            "timestamp", System.currentTimeMillis(),
            "alert", alert
        );
        
        messagingTemplate.convertAndSend("/topic/alerts", alertUpdate);
    }

    /**
     * Broadcast analytics update to all subscribers
     */
    public void broadcastAnalyticsUpdate(Map<String, Object> analytics) {
        log.debug("Broadcasting analytics update");
        
        Map<String, Object> update = Map.of(
            "type", "ANALYTICS_UPDATE",
            "timestamp", System.currentTimeMillis(),
            "data", analytics
        );
        
        messagingTemplate.convertAndSend("/topic/analytics", update);
    }
}
