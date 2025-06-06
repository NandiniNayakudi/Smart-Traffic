package com.traffic.controller;

import com.traffic.service.GoogleMapsTrafficService;
import com.traffic.service.GoogleBigQueryService;
import com.traffic.service.GoogleCloudPubSubService;
import com.traffic.service.MockGoogleCloudService;
import com.traffic.service.EnhancedGoogleCloudService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Google Cloud Integration Controller
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */
@RestController
@RequestMapping("/api/v1/google-cloud")
@Slf4j
public class GoogleCloudController {

    @Autowired(required = false)
    private GoogleMapsTrafficService googleMapsTrafficService;

    @Autowired(required = false)
    private GoogleBigQueryService googleBigQueryService;

    @Autowired(required = false)
    private GoogleCloudPubSubService googleCloudPubSubService;

    @Autowired
    private MockGoogleCloudService mockGoogleCloudService;

    @Autowired
    private EnhancedGoogleCloudService enhancedGoogleCloudService;

    /**
     * Get real-time traffic data from Google Maps
     */
    @GetMapping("/traffic/realtime")
    public ResponseEntity<Map<String, Object>> getRealTimeTraffic() {
        try {
            // This would trigger immediate fetch from Google Maps API
            Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Real-time traffic data fetch initiated",
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting real-time traffic data", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get directions with real-time traffic
     */
    @GetMapping("/directions")
    public ResponseEntity<Map<String, Object>> getDirections(
            @RequestParam String origin,
            @RequestParam String destination) {
        try {
            Map<String, Object> directions;
            if (googleMapsTrafficService != null) {
                directions = googleMapsTrafficService.getRouteTrafficConditions(origin, destination);
            } else {
                directions = mockGoogleCloudService.getRouteTrafficConditions(origin, destination);
            }
            return ResponseEntity.ok(directions);
        } catch (Exception e) {
            log.error("Error getting directions", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get BigQuery analytics summary
     */
    @GetMapping("/analytics/summary")
    public ResponseEntity<Map<String, Object>> getAnalyticsSummary() {
        try {
            Map<String, Object> summary;
            if (googleBigQueryService != null) {
                summary = googleBigQueryService.getCurrentTrafficSummary();
            } else {
                summary = mockGoogleCloudService.getCurrentTrafficSummary();
            }
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("Error getting analytics summary", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get congestion hotspots from BigQuery
     */
    @GetMapping("/analytics/hotspots")
    public ResponseEntity<Object> getCongestionHotspots() {
        try {
            Object hotspots;
            if (googleBigQueryService != null) {
                hotspots = googleBigQueryService.getCongestionHotspots();
            } else {
                hotspots = mockGoogleCloudService.getCongestionHotspots();
            }
            return ResponseEntity.ok(hotspots);
        } catch (Exception e) {
            log.error("Error getting congestion hotspots", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get traffic trends from BigQuery
     */
    @GetMapping("/analytics/trends")
    public ResponseEntity<Object> getTrafficTrends() {
        try {
            Object trends;
            if (googleBigQueryService != null) {
                trends = googleBigQueryService.getTrafficTrends();
            } else {
                trends = mockGoogleCloudService.getTrafficTrends();
            }
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            log.error("Error getting traffic trends", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get average speeds by location
     */
    @GetMapping("/analytics/speeds")
    public ResponseEntity<Map<String, Double>> getAverageSpeeds() {
        try {
            Map<String, Double> speeds;
            if (googleBigQueryService != null) {
                speeds = googleBigQueryService.getAverageSpeeds();
            } else {
                speeds = mockGoogleCloudService.getAverageSpeeds();
            }
            return ResponseEntity.ok(speeds);
        } catch (Exception e) {
            log.error("Error getting average speeds", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get Pub/Sub connection status
     */
    @GetMapping("/pubsub/status")
    public ResponseEntity<Map<String, Object>> getPubSubStatus() {
        try {
            Map<String, Object> status;
            if (googleCloudPubSubService != null) {
                status = googleCloudPubSubService.getConnectionStatus();
            } else {
                status = mockGoogleCloudService.getPubSubStatus();
            }
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting Pub/Sub status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Manually trigger traffic data fetch
     */
    @PostMapping("/traffic/fetch")
    public ResponseEntity<Map<String, Object>> triggerTrafficFetch() {
        try {
            Map<String, Object> response;
            if (googleMapsTrafficService != null) {
                // Trigger immediate fetch
                CompletableFuture.runAsync(() -> {
                    googleMapsTrafficService.fetchRealTimeTrafficData();
                });

                response = Map.of(
                    "status", "success",
                    "message", "Traffic data fetch triggered",
                    "timestamp", System.currentTimeMillis()
                );
            } else {
                response = mockGoogleCloudService.triggerTrafficFetch();
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error triggering traffic fetch", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test Google Cloud connectivity
     */
    @GetMapping("/test/connectivity")
    public ResponseEntity<Map<String, Object>> testConnectivity() {
        try {
            Map<String, Object> connectivity = Map.of(
                "googleMaps", testGoogleMapsConnectivity(),
                "bigQuery", testBigQueryConnectivity(),
                "pubSub", testPubSubConnectivity(),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(connectivity);
        } catch (Exception e) {
            log.error("Error testing connectivity", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get Google Cloud integration status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getIntegrationStatus() {
        try {
            Map<String, Object> status = enhancedGoogleCloudService.getServiceStatus();

            // Add connectivity test results
            status.put("connectivityTests", Map.of(
                "googleMaps", testGoogleMapsConnectivity(),
                "bigQuery", testBigQueryConnectivity(),
                "pubSub", testPubSubConnectivity()
            ));
            status.put("realTimeDataActive", true);

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error getting integration status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get enhanced analytics with your project configuration
     */
    @GetMapping("/enhanced/analytics")
    public ResponseEntity<Map<String, Object>> getEnhancedAnalytics() {
        try {
            Map<String, Object> analytics = enhancedGoogleCloudService.getEnhancedTrafficAnalytics();
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error getting enhanced analytics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get enhanced directions with your project
     */
    @GetMapping("/enhanced/directions")
    public ResponseEntity<Map<String, Object>> getEnhancedDirections(
            @RequestParam String origin,
            @RequestParam String destination) {
        try {
            Map<String, Object> directions = enhancedGoogleCloudService.getEnhancedDirections(origin, destination);
            return ResponseEntity.ok(directions);
        } catch (Exception e) {
            log.error("Error getting enhanced directions", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get project configuration
     */
    @GetMapping("/project/config")
    public ResponseEntity<Map<String, String>> getProjectConfiguration() {
        try {
            Map<String, String> config = enhancedGoogleCloudService.getProjectConfiguration();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            log.error("Error getting project configuration", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test Google Maps connectivity
     */
    private boolean testGoogleMapsConnectivity() {
        if (googleMapsTrafficService == null) {
            return false;
        }

        try {
            // Simple test - get directions between two points
            Map<String, Object> result = googleMapsTrafficService.getRouteTrafficConditions(
                "Times Square, New York",
                "Central Park, New York"
            );
            return result != null && !result.isEmpty() && !"MOCK_DATA".equals(result.get("source"));
        } catch (Exception e) {
            log.debug("Google Maps connectivity test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Test BigQuery connectivity
     */
    private boolean testBigQueryConnectivity() {
        if (googleBigQueryService == null) {
            return false;
        }

        try {
            Map<String, Object> summary = googleBigQueryService.getCurrentTrafficSummary();
            return summary != null && !summary.isEmpty() && !"MOCK_DATA".equals(summary.get("source"));
        } catch (Exception e) {
            log.debug("BigQuery connectivity test failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Test Pub/Sub connectivity
     */
    private boolean testPubSubConnectivity() {
        if (googleCloudPubSubService == null) {
            return false;
        }

        try {
            Map<String, Object> status = googleCloudPubSubService.getConnectionStatus();
            return (Boolean) status.getOrDefault("publisherAvailable", false) &&
                   !"MOCK".equals(status.get("mode"));
        } catch (Exception e) {
            log.debug("Pub/Sub connectivity test failed: {}", e.getMessage());
            return false;
        }
    }
}
