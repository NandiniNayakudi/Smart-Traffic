package com.traffic.controller;

import com.traffic.dto.*;
import com.traffic.model.TrafficData;
import com.traffic.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main controller for traffic management endpoints
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */
@RestController
@RequestMapping("/api/v1/traffic")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Traffic Management", description = "APIs for traffic data management and optimization")
public class TrafficController {

    private final TrafficIngestionService trafficIngestionService;
    private final PredictionService predictionService;
    private final RouteService routeService;
    private final SignalOptimizationService signalOptimizationService;
    private final TrendAnalysisService trendAnalysisService;

    /**
     * 1. Real-Time Traffic Data Ingestion
     */
    @PostMapping("/ingest")
    @Operation(summary = "Ingest real-time traffic data", 
               description = "Accepts real-time traffic data from external APIs or datasets")
    public ResponseEntity<TrafficData> ingestTrafficData(@Valid @RequestBody TrafficData trafficData) {
        log.info("Ingesting traffic data for location: {}", trafficData.getLocation());
        
        TrafficData savedData = trafficIngestionService.ingestTrafficData(trafficData);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedData);
    }

    /**
     * 2. Predictive Congestion Analysis
     */
    @GetMapping("/predict")
    @Operation(summary = "Predict traffic congestion", 
               description = "Uses trained ML model to predict traffic congestion at a given location and time")
    public ResponseEntity<PredictionResponse> predictTraffic(
            @Parameter(description = "Latitude") @RequestParam Double lat,
            @Parameter(description = "Longitude") @RequestParam Double lon,
            @Parameter(description = "Timestamp for prediction") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime timestamp) {
        
        log.info("Predicting traffic for coordinates: {}, {} at {}", lat, lon, timestamp);
        
        PredictionRequest request = new PredictionRequest(lat, lon, timestamp, null, null, null);
        PredictionResponse prediction = predictionService.predictTraffic(request);
        
        return ResponseEntity.ok(prediction);
    }

    /**
     * 3. Route Recommendation
     */
    @GetMapping("/route")
    @Operation(summary = "Get route recommendations", 
               description = "Suggests eco-friendly route from source to destination")
    public ResponseEntity<RouteResponse> getRouteRecommendation(
            @Parameter(description = "Source location") @RequestParam String source,
            @Parameter(description = "Destination location") @RequestParam String destination,
            @Parameter(description = "Enable eco-friendly routing") @RequestParam(defaultValue = "true") Boolean eco) {
        
        log.info("Getting route recommendation from {} to {} (eco: {})", source, destination, eco);
        
        RouteRequest request = new RouteRequest(source, destination, eco, "DRIVING", false, false);
        RouteResponse route = routeService.getOptimalRoute(request);
        
        return ResponseEntity.ok(route);
    }

    /**
     * 4. Traffic Signal Optimization
     */
    @PostMapping("/signal/optimize")
    @Operation(summary = "Optimize traffic signals", 
               description = "Accepts live intersection data and returns optimized signal timings")
    public ResponseEntity<SignalOptimizationResponse> optimizeSignal(
            @Valid @RequestBody SignalOptimizationRequest request) {
        
        log.info("Optimizing signal for intersection: {}", request.getIntersectionId());
        
        SignalOptimizationResponse response = signalOptimizationService.optimizeSignal(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 5. Historical Traffic Trends
     */
    @GetMapping("/trends")
    @Operation(summary = "Get traffic trends", 
               description = "Provides average traffic data trends over time for city planners")
    public ResponseEntity<TrendAnalysisResponse> getTrafficTrends(
            @Parameter(description = "Location name") @RequestParam String location,
            @Parameter(description = "Analysis period") @RequestParam(defaultValue = "monthly") String period) {
        
        log.info("Getting traffic trends for location: {} with period: {}", location, period);
        
        TrendAnalysisResponse trends = trendAnalysisService.getTrafficTrends(location, period);
        return ResponseEntity.ok(trends);
    }

    /**
     * 6. Manual ML Model Training
     */
    @PostMapping("/train")
    @Operation(summary = "Trigger ML model training", 
               description = "Manually trigger re-training of the ML model")
    public ResponseEntity<ModelTrainingResponse> trainModel() {
        log.info("Triggering ML model training");
        
        ModelTrainingResponse response = predictionService.triggerModelTraining();
        return ResponseEntity.ok(response);
    }

    /**
     * Get all traffic data for a location
     */
    @GetMapping("/data")
    @Operation(summary = "Get traffic data",
               description = "Retrieve traffic data for a specific location")
    public ResponseEntity<List<TrafficData>> getTrafficData(
            @Parameter(description = "Location name") @RequestParam(required = false) String location,
            @Parameter(description = "Limit results") @RequestParam(defaultValue = "100") Integer limit) {

        log.info("Getting traffic data for location: {} (limit: {})", location, limit);

        List<TrafficData> data = trafficIngestionService.getTrafficData(location, limit);
        return ResponseEntity.ok(data);
    }

    /**
     * Get current traffic conditions (for frontend dashboard)
     */
    @GetMapping("/current")
    @Operation(summary = "Get current traffic conditions",
               description = "Retrieve current real-time traffic conditions for dashboard")
    public ResponseEntity<List<TrafficData>> getCurrentTrafficConditions() {
        log.info("Getting current traffic conditions");

        try {
            List<TrafficData> currentData = trafficIngestionService.getCurrentTrafficData();
            return ResponseEntity.ok(currentData);
        } catch (Exception e) {
            log.error("Error fetching current traffic data", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get traffic statistics (for frontend dashboard)
     */
    @GetMapping("/stats")
    @Operation(summary = "Get traffic statistics",
               description = "Retrieve traffic statistics and metrics for dashboard")
    public ResponseEntity<java.util.Map<String, Object>> getTrafficStats() {
        log.info("Getting traffic statistics");

        try {
            java.util.Map<String, Object> stats = trafficIngestionService.getTrafficStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching traffic statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(summary = "Traffic service health check",
               description = "Check the health status of traffic management service")
    public ResponseEntity<java.util.Map<String, Object>> healthCheck() {
        log.info("Traffic service health check requested");

        try {
            java.util.Map<String, Object> health = java.util.Map.of(
                "status", "UP",
                "service", "Traffic Management",
                "timestamp", LocalDateTime.now(),
                "components", java.util.Map.of(
                    "dataIngestion", "ACTIVE",
                    "prediction", "RUNNING",
                    "routing", "OPERATIONAL",
                    "signalOptimization", "ACTIVE"
                )
            );
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Health check failed", e);
            return ResponseEntity.status(503).body(java.util.Map.of(
                "status", "DOWN",
                "error", e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
}
