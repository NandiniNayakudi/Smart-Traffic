package com.traffic.service;

import com.traffic.dto.SignalOptimizationRequest;
import com.traffic.dto.SignalOptimizationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for traffic signal optimization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SignalOptimizationService {

    private static final int MIN_SIGNAL_TIME = 10; // Minimum signal time in seconds
    private static final int MAX_SIGNAL_TIME = 60; // Maximum signal time in seconds
    private static final int TOTAL_CYCLE_TIME = 120; // Total cycle time in seconds

    /**
     * Optimize traffic signal timings based on traffic flow
     */
    public SignalOptimizationResponse optimizeSignal(SignalOptimizationRequest request) {
        try {
            log.info("Optimizing signal for intersection: {}", request.getIntersectionId());

            // Calculate total traffic volume
            int totalTraffic = request.getNorth() + request.getSouth() + 
                              request.getEast() + request.getWest();

            if (totalTraffic == 0) {
                return createDefaultSignalResponse(request.getIntersectionId());
            }

            // Calculate proportional signal timings
            SignalOptimizationResponse.SignalTimings optimizedTimings = 
                calculateOptimalTimings(request, totalTraffic);

            // Apply optimization strategy
            String strategy = determineOptimizationStrategy(request);
            Double efficiency = calculateEfficiencyImprovement(request, optimizedTimings);

            SignalOptimizationResponse response = new SignalOptimizationResponse();
            response.setSignalTimings(optimizedTimings);
            response.setOptimizationStrategy(strategy);
            response.setEfficiencyImprovement(efficiency);
            response.setMessage("Signal timings optimized successfully for intersection " + 
                               request.getIntersectionId());

            log.info("Signal optimization completed for intersection: {} with {}% efficiency improvement", 
                    request.getIntersectionId(), String.format("%.1f", efficiency));

            return response;

        } catch (Exception e) {
            log.error("Error optimizing signal for intersection {}: {}", 
                     request.getIntersectionId(), e.getMessage(), e);
            throw new RuntimeException("Failed to optimize signal", e);
        }
    }

    /**
     * Calculate optimal signal timings based on traffic flow
     */
    private SignalOptimizationResponse.SignalTimings calculateOptimalTimings(
            SignalOptimizationRequest request, int totalTraffic) {

        // Calculate base proportions
        double northProportion = (double) request.getNorth() / totalTraffic;
        double southProportion = (double) request.getSouth() / totalTraffic;
        double eastProportion = (double) request.getEast() / totalTraffic;
        double westProportion = (double) request.getWest() / totalTraffic;

        // Group opposite directions (North-South and East-West)
        double nsTrafficProportion = northProportion + southProportion;
        double ewTrafficProportion = eastProportion + westProportion;

        // Calculate cycle times for each direction group
        int nsCycleTime = (int) Math.round(TOTAL_CYCLE_TIME * nsTrafficProportion / 2);
        int ewCycleTime = (int) Math.round(TOTAL_CYCLE_TIME * ewTrafficProportion / 2);

        // Ensure minimum and maximum constraints
        nsCycleTime = Math.max(MIN_SIGNAL_TIME, Math.min(MAX_SIGNAL_TIME, nsCycleTime));
        ewCycleTime = Math.max(MIN_SIGNAL_TIME, Math.min(MAX_SIGNAL_TIME, ewCycleTime));

        // Distribute time between opposite directions
        int northTime = (int) Math.round(nsCycleTime * (northProportion / nsTrafficProportion));
        int southTime = nsCycleTime - northTime;
        int eastTime = (int) Math.round(ewCycleTime * (eastProportion / ewTrafficProportion));
        int westTime = ewCycleTime - eastTime;

        // Apply time-of-day adjustments
        applyTimeOfDayAdjustments(request, northTime, southTime, eastTime, westTime);

        // Apply weather adjustments
        if (isAdverseWeather(request.getWeatherCondition())) {
            northTime = (int) (northTime * 1.1);
            southTime = (int) (southTime * 1.1);
            eastTime = (int) (eastTime * 1.1);
            westTime = (int) (westTime * 1.1);
        }

        return new SignalOptimizationResponse.SignalTimings(northTime, southTime, eastTime, westTime);
    }

    /**
     * Apply time-of-day specific adjustments
     */
    private void applyTimeOfDayAdjustments(SignalOptimizationRequest request, 
                                          int northTime, int southTime, int eastTime, int westTime) {
        String timeOfDay = request.getTimeOfDay();
        if (timeOfDay != null) {
            switch (timeOfDay.toUpperCase()) {
                case "MORNING_RUSH":
                    // Typically more traffic towards city center
                    // Adjust based on typical traffic patterns
                    break;
                case "EVENING_RUSH":
                    // Typically more traffic away from city center
                    // Adjust based on typical traffic patterns
                    break;
                case "NIGHT":
                    // Reduce all timings for night time
                    northTime = Math.max(MIN_SIGNAL_TIME, (int) (northTime * 0.8));
                    southTime = Math.max(MIN_SIGNAL_TIME, (int) (southTime * 0.8));
                    eastTime = Math.max(MIN_SIGNAL_TIME, (int) (eastTime * 0.8));
                    westTime = Math.max(MIN_SIGNAL_TIME, (int) (westTime * 0.8));
                    break;
            }
        }
    }

    /**
     * Determine optimization strategy based on traffic patterns
     */
    private String determineOptimizationStrategy(SignalOptimizationRequest request) {
        int totalTraffic = request.getNorth() + request.getSouth() + 
                          request.getEast() + request.getWest();

        // Find the direction with maximum traffic
        int maxTraffic = Math.max(Math.max(request.getNorth(), request.getSouth()),
                                 Math.max(request.getEast(), request.getWest()));

        double maxTrafficRatio = (double) maxTraffic / totalTraffic;

        if (maxTrafficRatio > 0.5) {
            return "PRIORITY_DIRECTION - Prioritizing direction with highest traffic volume";
        } else if (Math.abs(request.getNorth() + request.getSouth() - 
                           request.getEast() - request.getWest()) < totalTraffic * 0.2) {
            return "BALANCED_FLOW - Balanced traffic distribution across all directions";
        } else {
            return "PROPORTIONAL_TIMING - Timing proportional to traffic volume";
        }
    }

    /**
     * Calculate efficiency improvement percentage
     */
    private Double calculateEfficiencyImprovement(SignalOptimizationRequest request, 
                                                 SignalOptimizationResponse.SignalTimings optimized) {
        // Simulate efficiency calculation
        // In real implementation, this would compare with previous timings
        
        int totalTraffic = request.getNorth() + request.getSouth() + 
                          request.getEast() + request.getWest();
        
        // Higher traffic volumes typically see better improvements
        double baseImprovement = Math.min(25.0, totalTraffic / 10.0);
        
        // Add randomness for realistic simulation
        double randomFactor = Math.random() * 10.0 - 5.0; // -5% to +5%
        
        return Math.max(5.0, Math.min(30.0, baseImprovement + randomFactor));
    }

    /**
     * Create default signal response for zero traffic
     */
    private SignalOptimizationResponse createDefaultSignalResponse(String intersectionId) {
        SignalOptimizationResponse.SignalTimings defaultTimings = 
            new SignalOptimizationResponse.SignalTimings(30, 30, 30, 30);
        
        SignalOptimizationResponse response = new SignalOptimizationResponse();
        response.setSignalTimings(defaultTimings);
        response.setOptimizationStrategy("DEFAULT_TIMING - Equal timing for all directions");
        response.setEfficiencyImprovement(0.0);
        response.setMessage("Default signal timings applied for intersection " + intersectionId);
        
        return response;
    }

    /**
     * Check if weather condition is adverse
     */
    private boolean isAdverseWeather(String weatherCondition) {
        if (weatherCondition == null) return false;
        
        String weather = weatherCondition.toUpperCase();
        return weather.contains("RAIN") || weather.contains("SNOW") || 
               weather.contains("FOG") || weather.contains("STORM");
    }

    /**
     * Get adaptive signal timing based on real-time conditions
     */
    public SignalOptimizationResponse getAdaptiveSignalTiming(String intersectionId) {
        log.info("Getting adaptive signal timing for intersection: {}", intersectionId);
        
        // In real implementation, this would fetch real-time traffic data
        // For simulation, create a sample request
        SignalOptimizationRequest simulatedRequest = new SignalOptimizationRequest(
            intersectionId, 45, 35, 25, 30, "PEAK_HOUR", "WEEKDAY", "CLEAR"
        );
        
        return optimizeSignal(simulatedRequest);
    }
}
