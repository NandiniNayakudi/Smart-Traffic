package com.traffic.service;

import com.traffic.dto.ModelTrainingResponse;
import com.traffic.dto.PredictionRequest;
import com.traffic.dto.PredictionResponse;
import com.traffic.model.TrafficData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Service for ML-based traffic prediction
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

    private final TrafficIngestionService trafficIngestionService;
    private final WebClient.Builder webClientBuilder;
    
    @Value("${ml.model.endpoint}")
    private String mlModelEndpoint;
    
    @Value("${ml.model.timeout}")
    private int mlModelTimeout;

    /**
     * Predict traffic congestion using ML model
     */
    public PredictionResponse predictTraffic(PredictionRequest request) {
        try {
            // Validate input parameters
            if (request.getLat() == null || request.getLon() == null) {
                throw new RuntimeException("Latitude and longitude are required");
            }

            log.info("Predicting traffic for coordinates: {}, {} at {}",
                    request.getLat(), request.getLon(), request.getTimestamp());

            // Enrich request with additional features
            enrichPredictionRequest(request);

            // Get historical data for the location
            List<TrafficData> historicalData = trafficIngestionService.getRecentTrafficData(
                    request.getLat(), request.getLon(), 24);

            // Try to call external ML model first
            try {
                return callExternalMLModel(request, historicalData);
            } catch (Exception e) {
                log.warn("External ML model unavailable, falling back to rule-based prediction: {}", e.getMessage());
                return performRuleBasedPrediction(request, historicalData);
            }

        } catch (Exception e) {
            log.error("Error predicting traffic: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to predict traffic", e);
        }
    }

    /**
     * Trigger ML model training
     */
    public ModelTrainingResponse triggerModelTraining() {
        try {
            log.info("Triggering ML model training");
            
            // In a real implementation, this would trigger actual model training
            // For now, we'll simulate the training process
            
            String modelId = "v" + System.currentTimeMillis();
            
            // Simulate training time
            Thread.sleep(1000);
            
            log.info("ML model training completed with model ID: {}", modelId);
            
            return new ModelTrainingResponse("Training Started", modelId, "Model training initiated successfully");
            
        } catch (Exception e) {
            log.error("Error triggering model training: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to trigger model training", e);
        }
    }

    /**
     * Call external ML model for prediction
     */
    private PredictionResponse callExternalMLModel(PredictionRequest request, List<TrafficData> historicalData) {
        try {
            WebClient webClient = webClientBuilder.build();
            
            // Prepare ML model request payload
            MLModelRequest mlRequest = new MLModelRequest(
                    request.getLat(),
                    request.getLon(),
                    request.getHour(),
                    request.getDayOfWeek(),
                    request.getWeatherCondition(),
                    historicalData.size()
            );

            // Call external ML service
            MLModelResponse mlResponse = webClient.post()
                    .uri(mlModelEndpoint)
                    .bodyValue(mlRequest)
                    .retrieve()
                    .bodyToMono(MLModelResponse.class)
                    .timeout(java.time.Duration.ofMillis(mlModelTimeout))
                    .block();

            if (mlResponse != null) {
                TrafficData.TrafficDensity prediction = TrafficData.TrafficDensity.valueOf(mlResponse.prediction());
                return new PredictionResponse(prediction, mlResponse.confidence());
            } else {
                throw new RuntimeException("Empty response from ML model");
            }

        } catch (Exception e) {
            log.error("Error calling external ML model: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Perform rule-based prediction as fallback
     */
    private PredictionResponse performRuleBasedPrediction(PredictionRequest request, List<TrafficData> historicalData) {
        log.info("Performing rule-based traffic prediction");

        TrafficData.TrafficDensity prediction;
        double confidence;

        // Rule-based prediction logic
        int hour = request.getHour() != null ? request.getHour() : request.getTimestamp().getHour();
        String dayOfWeek = request.getDayOfWeek() != null ? request.getDayOfWeek() : 
                          request.getTimestamp().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        // Peak hours logic
        if (isPeakHour(hour)) {
            if (isWeekday(dayOfWeek)) {
                prediction = TrafficData.TrafficDensity.HIGH;
                confidence = 0.85;
            } else {
                prediction = TrafficData.TrafficDensity.MODERATE;
                confidence = 0.75;
            }
        } else if (isOffPeakHour(hour)) {
            prediction = TrafficData.TrafficDensity.LOW;
            confidence = 0.80;
        } else {
            prediction = TrafficData.TrafficDensity.MODERATE;
            confidence = 0.70;
        }

        // Adjust based on historical data
        if (!historicalData.isEmpty()) {
            double avgDensityScore = historicalData.stream()
                    .mapToDouble(data -> getDensityScore(data.getTrafficDensity()))
                    .average()
                    .orElse(2.0);

            if (avgDensityScore > 2.5) {
                prediction = increaseTrafficDensity(prediction);
                confidence += 0.05;
            } else if (avgDensityScore < 1.5) {
                prediction = decreaseTrafficDensity(prediction);
                confidence += 0.05;
            }
        }

        // Weather impact
        if ("RAIN".equalsIgnoreCase(request.getWeatherCondition()) || 
            "SNOW".equalsIgnoreCase(request.getWeatherCondition())) {
            prediction = increaseTrafficDensity(prediction);
            confidence -= 0.10;
        }

        // Ensure confidence is within bounds
        confidence = Math.max(0.5, Math.min(0.95, confidence));

        log.info("Rule-based prediction: {} with confidence: {}", prediction, confidence);
        return new PredictionResponse(prediction, confidence);
    }

    /**
     * Enrich prediction request with additional features
     */
    private void enrichPredictionRequest(PredictionRequest request) {
        if (request.getHour() == null) {
            request.setHour(request.getTimestamp().getHour());
        }
        
        if (request.getDayOfWeek() == null) {
            request.setDayOfWeek(request.getTimestamp().getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }
        
        if (request.getWeatherCondition() == null) {
            request.setWeatherCondition("CLEAR");
        }
    }

    private boolean isPeakHour(int hour) {
        return (hour >= 7 && hour <= 9) || (hour >= 17 && hour <= 19);
    }

    private boolean isOffPeakHour(int hour) {
        return hour >= 22 || hour <= 6;
    }

    private boolean isWeekday(String dayOfWeek) {
        return !("Saturday".equalsIgnoreCase(dayOfWeek) || "Sunday".equalsIgnoreCase(dayOfWeek));
    }

    private double getDensityScore(TrafficData.TrafficDensity density) {
        return switch (density) {
            case LOW -> 1.0;
            case MODERATE -> 2.0;
            case HIGH -> 3.0;
            case CRITICAL -> 4.0;
        };
    }

    private TrafficData.TrafficDensity increaseTrafficDensity(TrafficData.TrafficDensity current) {
        return switch (current) {
            case LOW -> TrafficData.TrafficDensity.MODERATE;
            case MODERATE -> TrafficData.TrafficDensity.HIGH;
            case HIGH, CRITICAL -> TrafficData.TrafficDensity.CRITICAL;
        };
    }

    private TrafficData.TrafficDensity decreaseTrafficDensity(TrafficData.TrafficDensity current) {
        return switch (current) {
            case CRITICAL -> TrafficData.TrafficDensity.HIGH;
            case HIGH -> TrafficData.TrafficDensity.MODERATE;
            case MODERATE, LOW -> TrafficData.TrafficDensity.LOW;
        };
    }

    // Inner classes for ML model communication
    private record MLModelRequest(Double lat, Double lon, Integer hour, String dayOfWeek, 
                                 String weather, Integer historicalDataPoints) {}
    
    private record MLModelResponse(String prediction, Double confidence) {}
}
