package com.traffic.service;

import com.traffic.model.*;
import com.traffic.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for AI Analytics and Machine Learning operations
 * Part of Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 * Department of Computer Science and Engineering, Amrita School of Engineering, Amritapuri, Kerala
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AIAnalyticsService {
    
    private final TrafficPredictionRepository predictionRepository;
    private final MLModelMetricsRepository modelMetricsRepository;
    private final BigQueryAnalyticsRepository bigQueryRepository;
    private final EcoRouteRepository ecoRouteRepository;
    private final TrafficDataRepository trafficDataRepository;
    
    /**
     * Get comprehensive AI analytics dashboard data
     */
    public Map<String, Object> getAIDashboardData() {
        log.info("Generating AI analytics dashboard data");
        
        Map<String, Object> dashboardData = new HashMap<>();
        
        // Model Performance Metrics
        dashboardData.put("modelMetrics", getModelPerformanceMetrics());
        
        // Prediction Analytics
        dashboardData.put("predictionAnalytics", getPredictionAnalytics());
        
        // BigQuery Analytics
        dashboardData.put("bigQueryAnalytics", getBigQueryAnalytics());
        
        // Eco Route Analytics
        dashboardData.put("ecoRouteAnalytics", getEcoRouteAnalytics());
        
        // Real-time AI Status
        dashboardData.put("aiStatus", getAISystemStatus());
        
        return dashboardData;
    }
    
    /**
     * Get model performance metrics
     */
    public Map<String, Object> getModelPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get latest version of each model
        List<MLModelMetrics> latestModels = modelMetricsRepository.findLatestVersionOfEachModel();
        
        // Calculate overall performance
        double avgAccuracy = latestModels.stream()
            .filter(m -> m.getAccuracyScore() != null)
            .mapToDouble(MLModelMetrics::getAccuracyScore)
            .average()
            .orElse(0.0);
        
        // Count models by status
        Map<MLModelMetrics.ModelStatus, Long> statusCounts = latestModels.stream()
            .collect(Collectors.groupingBy(MLModelMetrics::getModelStatus, Collectors.counting()));
        
        // Get production-ready models
        List<MLModelMetrics> productionModels = modelMetricsRepository.findProductionReadyModels();
        
        metrics.put("totalModels", latestModels.size());
        metrics.put("averageAccuracy", Math.round(avgAccuracy * 100.0) / 100.0);
        metrics.put("productionModels", productionModels.size());
        metrics.put("statusDistribution", statusCounts);
        metrics.put("latestModels", latestModels.stream().limit(5).collect(Collectors.toList()));
        
        return metrics;
    }
    
    /**
     * Get prediction analytics
     */
    public Map<String, Object> getPredictionAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        
        // Recent predictions
        List<TrafficPrediction> recentPredictions = predictionRepository.findPredictionsInTimeRange(
            since, LocalDateTime.now().plusHours(1));
        
        // High confidence predictions
        List<TrafficPrediction> highConfidencePredictions = predictionRepository
            .findHighConfidencePredictions(0.8);
        
        // Predictions for next hour
        List<TrafficPrediction> nextHourPredictions = predictionRepository
            .findPredictionsForNextHour(LocalDateTime.now(), LocalDateTime.now().plusHours(1));
        
        // Calculate average confidence
        double avgConfidence = recentPredictions.stream()
            .filter(p -> p.getConfidenceScore() != null)
            .mapToDouble(TrafficPrediction::getConfidenceScore)
            .average()
            .orElse(0.0);
        
        analytics.put("totalPredictions", recentPredictions.size());
        analytics.put("averageConfidence", Math.round(avgConfidence * 100.0) / 100.0);
        analytics.put("highConfidencePredictions", highConfidencePredictions.size());
        analytics.put("nextHourPredictions", nextHourPredictions.size());
        analytics.put("recentPredictions", recentPredictions.stream().limit(10).collect(Collectors.toList()));
        
        return analytics;
    }
    
    /**
     * Get BigQuery analytics
     */
    public Map<String, Object> getBigQueryAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        
        // Recent queries
        List<BigQueryAnalytics> recentQueries = bigQueryRepository.findRecentQueries(since);
        
        // Query statistics
        List<Object[]> queryStats = bigQueryRepository.getQueryStatsByType();
        
        // Cache hit rate
        Optional<Double> cacheHitRate = bigQueryRepository.getCacheHitRate(since);
        
        // Failed queries
        List<BigQueryAnalytics> failedQueries = bigQueryRepository.findFailedQueries();
        
        // Calculate total cost
        double totalCost = recentQueries.stream()
            .filter(q -> q.getTotalCostUsd() != null)
            .mapToDouble(BigQueryAnalytics::getTotalCostUsd)
            .sum();
        
        analytics.put("totalQueries", recentQueries.size());
        analytics.put("totalCost", Math.round(totalCost * 100.0) / 100.0);
        analytics.put("cacheHitRate", cacheHitRate.orElse(0.0));
        analytics.put("failedQueries", failedQueries.size());
        analytics.put("queryStats", queryStats);
        analytics.put("recentQueries", recentQueries.stream().limit(5).collect(Collectors.toList()));
        
        return analytics;
    }
    
    /**
     * Get eco route analytics
     */
    public Map<String, Object> getEcoRouteAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        
        // Recent eco routes
        List<EcoRoute> recentRoutes = ecoRouteRepository.findRecentRoutes(since);
        
        // SDG 11 contributing routes
        List<EcoRoute> sdgRoutes = ecoRouteRepository.findSDG11ContributingRoutes(70.0);
        
        // Calculate total CO2 savings
        Optional<Double> totalEmissions = ecoRouteRepository.getTotalCO2Emissions(since);
        
        // Environmental impact distribution
        List<Object[]> impactDistribution = ecoRouteRepository.getEnvironmentalImpactDistribution();
        
        // Average eco score
        double avgEcoScore = recentRoutes.stream()
            .filter(r -> r.getEcoScore() != null)
            .mapToDouble(EcoRoute::getEcoScore)
            .average()
            .orElse(0.0);
        
        analytics.put("totalRoutes", recentRoutes.size());
        analytics.put("averageEcoScore", Math.round(avgEcoScore * 100.0) / 100.0);
        analytics.put("sdgContributingRoutes", sdgRoutes.size());
        analytics.put("totalCO2Emissions", totalEmissions.orElse(0.0));
        analytics.put("impactDistribution", impactDistribution);
        analytics.put("recentRoutes", recentRoutes.stream().limit(5).collect(Collectors.toList()));
        
        return analytics;
    }
    
    /**
     * Get AI system status
     */
    public Map<String, Object> getAISystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // Google Cloud AI status (simulated)
        status.put("googleCloudAI", Map.of(
            "status", "ACTIVE",
            "modelsDeployed", modelMetricsRepository.findByModelStatusOrderByAccuracyScoreDesc(MLModelMetrics.ModelStatus.DEPLOYED).size(),
            "lastUpdate", LocalDateTime.now().minusMinutes(2)
        ));
        
        // BigQuery status
        status.put("bigQuery", Map.of(
            "status", "ACTIVE",
            "queriesLast24h", bigQueryRepository.findRecentQueries(LocalDateTime.now().minusHours(24)).size(),
            "lastQuery", LocalDateTime.now().minusMinutes(1)
        ));
        
        // ML Pipeline status
        status.put("mlPipeline", Map.of(
            "status", "RUNNING",
            "modelsTraining", modelMetricsRepository.findModelsInTraining().size(),
            "predictionsPerMinute", calculatePredictionsPerMinute()
        ));
        
        return status;
    }
    
    /**
     * Generate traffic predictions using AI models
     */
    public List<TrafficPrediction> generateTrafficPredictions(String location, int hoursAhead) {
        log.info("Generating traffic predictions for location: {} for next {} hours", location, hoursAhead);
        
        List<TrafficPrediction> predictions = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        // Simulate AI-powered predictions
        for (int i = 1; i <= hoursAhead; i++) {
            TrafficPrediction prediction = new TrafficPrediction();
            prediction.setLocation(location);
            prediction.setLatitude(40.7128 + Math.random() * 0.01); // NYC area simulation
            prediction.setLongitude(-74.0060 + Math.random() * 0.01);
            prediction.setPredictionTime(now);
            prediction.setPredictedForTime(now.plusHours(i));
            
            // Simulate prediction based on time of day
            TrafficData.TrafficDensity density = predictDensityForTime(now.plusHours(i));
            prediction.setPredictedDensity(density);
            
            // Simulate confidence score
            prediction.setConfidenceScore(0.75 + Math.random() * 0.2);
            
            // Simulate other metrics
            prediction.setPredictedVehicleCount((int) (100 + Math.random() * 200));
            prediction.setPredictedAverageSpeed(25.0 + Math.random() * 20.0);
            prediction.setCongestionProbability(Math.random());
            prediction.setModelVersion("v2.1.0");
            prediction.setMlAlgorithm("Random Forest");
            prediction.setGoogleCloudJobId("job-" + UUID.randomUUID().toString().substring(0, 8));
            prediction.setBigqueryDataset("traffic_data");
            
            predictions.add(prediction);
        }
        
        return predictionRepository.saveAll(predictions);
    }
    
    /**
     * Calculate eco-friendly routes
     */
    public List<EcoRoute> calculateEcoRoutes(String origin, String destination) {
        log.info("Calculating eco-friendly routes from {} to {}", origin, destination);
        
        List<EcoRoute> routes = new ArrayList<>();
        
        // Generate different route options
        String[] routeTypes = {"ECO_OPTIMAL", "TRAFFIC_OPTIMAL", "BALANCED"};
        
        for (String type : routeTypes) {
            EcoRoute route = new EcoRoute();
            route.setRouteName(type + " Route");
            route.setOrigin(origin);
            route.setDestination(destination);
            route.setOriginLat(40.7128 + Math.random() * 0.01);
            route.setOriginLng(-74.0060 + Math.random() * 0.01);
            route.setDestinationLat(40.7589 + Math.random() * 0.01);
            route.setDestinationLng(-73.9851 + Math.random() * 0.01);
            
            // Simulate route metrics
            route.setDistanceKm(5.0 + Math.random() * 10.0);
            route.setEstimatedDurationMinutes((int) (20 + Math.random() * 30));
            route.setCo2EmissionsKg(1.5 + Math.random() * 2.0);
            route.setFuelConsumptionLiters(0.5 + Math.random() * 1.0);
            
            // Calculate eco score based on type
            double ecoScore = switch (type) {
                case "ECO_OPTIMAL" -> 85.0 + Math.random() * 10.0;
                case "TRAFFIC_OPTIMAL" -> 60.0 + Math.random() * 15.0;
                case "BALANCED" -> 75.0 + Math.random() * 10.0;
                default -> 70.0;
            };
            route.setEcoScore(ecoScore);
            
            route.setTrafficEfficiencyScore(70.0 + Math.random() * 20.0);
            route.setAirQualityImpact(0.7 + Math.random() * 0.2);
            route.setRouteType(EcoRoute.RouteType.valueOf(type));
            route.setOptimizationPriority(EcoRoute.OptimizationPriority.BALANCE_ALL);
            route.setSdg11ContributionScore(ecoScore * 0.9);
            route.setGoogleMapsRouteId("route-" + UUID.randomUUID().toString().substring(0, 8));
            
            routes.add(route);
        }
        
        return ecoRouteRepository.saveAll(routes);
    }
    
    /**
     * Update ML model metrics
     */
    public MLModelMetrics updateModelMetrics(String modelName, Map<String, Object> metrics) {
        log.info("Updating metrics for model: {}", modelName);
        
        MLModelMetrics modelMetrics = new MLModelMetrics();
        modelMetrics.setModelName(modelName);
        modelMetrics.setModelVersion("v" + (Math.random() * 3 + 1));
        modelMetrics.setModelType(MLModelMetrics.ModelType.TRAFFIC_PREDICTION);
        modelMetrics.setModelStatus(MLModelMetrics.ModelStatus.DEPLOYED);
        
        // Set metrics from input
        if (metrics.containsKey("accuracy")) {
            modelMetrics.setAccuracyScore((Double) metrics.get("accuracy"));
        }
        if (metrics.containsKey("precision")) {
            modelMetrics.setPrecisionScore((Double) metrics.get("precision"));
        }
        if (metrics.containsKey("recall")) {
            modelMetrics.setRecallScore((Double) metrics.get("recall"));
        }
        
        // Simulate other metrics
        modelMetrics.setF1Score(0.85 + Math.random() * 0.1);
        modelMetrics.setPredictionsPerMinute((int) (100 + Math.random() * 500));
        modelMetrics.setInferenceLatencyMs(50.0 + Math.random() * 50.0);
        modelMetrics.setGoogleCloudModelId("model-" + UUID.randomUUID().toString().substring(0, 8));
        modelMetrics.setBigqueryDataset("ml_models");
        modelMetrics.setDeployedAt(LocalDateTime.now());
        modelMetrics.setLastPredictionAt(LocalDateTime.now());
        
        return modelMetricsRepository.save(modelMetrics);
    }
    
    // Helper methods
    
    private TrafficData.TrafficDensity predictDensityForTime(LocalDateTime time) {
        int hour = time.getHour();
        
        // Rush hour logic
        if ((hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19)) {
            return Math.random() > 0.3 ? TrafficData.TrafficDensity.HIGH : TrafficData.TrafficDensity.CRITICAL;
        } else if (hour >= 10 && hour <= 16) {
            return Math.random() > 0.5 ? TrafficData.TrafficDensity.MODERATE : TrafficData.TrafficDensity.LOW;
        } else {
            return Math.random() > 0.7 ? TrafficData.TrafficDensity.LOW : TrafficData.TrafficDensity.MODERATE;
        }
    }
    
    private int calculatePredictionsPerMinute() {
        // Simulate real-time prediction rate
        return (int) (200 + Math.random() * 300);
    }
}
