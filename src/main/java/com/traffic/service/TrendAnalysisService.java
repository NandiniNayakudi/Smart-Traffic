package com.traffic.service;

import com.traffic.dto.TrendAnalysisResponse;
import com.traffic.model.TrafficData;
import com.traffic.repository.TrafficDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for traffic trend analysis and historical data processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TrendAnalysisService {

    private final TrafficDataRepository trafficDataRepository;

    /**
     * Get traffic trends for a location and period
     */
    public TrendAnalysisResponse getTrafficTrends(String location, String period) {
        try {
            log.info("Analyzing traffic trends for location: {} with period: {}", location, period);

            // Get historical data
            List<TrafficData> historicalData = getHistoricalData(location, period);
            
            if (historicalData.isEmpty()) {
                return createEmptyTrendResponse(location, period);
            }

            // Analyze trends based on period
            TrendAnalysisResponse response = new TrendAnalysisResponse();
            response.setLocation(location);
            response.setPeriod(period);

            switch (period.toLowerCase()) {
                case "monthly":
                    response.setMonthlyTrend(analyzeMonthlyTrends(historicalData));
                    break;
                case "weekly":
                    response.setWeeklyTrend(analyzeWeeklyTrends(historicalData));
                    break;
                case "daily":
                    response.setDailyTrend(analyzeDailyTrends(historicalData));
                    break;
                default:
                    // Default to monthly
                    response.setMonthlyTrend(analyzeMonthlyTrends(historicalData));
                    response.setWeeklyTrend(analyzeWeeklyTrends(historicalData));
                    response.setDailyTrend(analyzeDailyTrends(historicalData));
            }

            // Generate summary
            response.setSummary(generateTrendSummary(historicalData));

            log.info("Traffic trend analysis completed for location: {}", location);
            return response;

        } catch (Exception e) {
            log.error("Error analyzing traffic trends: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to analyze traffic trends", e);
        }
    }

    /**
     * Get historical traffic data based on location and period
     */
    private List<TrafficData> getHistoricalData(String location, String period) {
        LocalDateTime startDate = calculateStartDate(period);
        
        if (location != null && !location.trim().isEmpty()) {
            return trafficDataRepository.findByLocationContainingIgnoreCaseAndTimestampAfterOrderByTimestampDesc(
                    location.trim(), startDate);
        } else {
            return trafficDataRepository.findByTimestampAfterOrderByTimestampDesc(startDate);
        }
    }

    /**
     * Calculate start date based on analysis period
     */
    private LocalDateTime calculateStartDate(String period) {
        LocalDateTime now = LocalDateTime.now();
        
        return switch (period.toLowerCase()) {
            case "daily" -> now.minusDays(7);
            case "weekly" -> now.minusWeeks(12);
            case "monthly" -> now.minusMonths(12);
            default -> now.minusMonths(6);
        };
    }

    /**
     * Analyze monthly traffic trends
     */
    private List<TrendAnalysisResponse.TrendData> analyzeMonthlyTrends(List<TrafficData> data) {
        Map<String, List<TrafficData>> monthlyData = data.stream()
                .collect(Collectors.groupingBy(d -> 
                    d.getTimestamp().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)));

        return monthlyData.entrySet().stream()
                .map(entry -> {
                    String month = entry.getKey();
                    List<TrafficData> monthData = entry.getValue();
                    
                    String avgDensity = calculateAverageTrafficDensity(monthData);
                    Double avgVehicleCount = calculateAverageVehicleCount(monthData);
                    Double avgSpeed = calculateAverageSpeed(monthData);
                    
                    return new TrendAnalysisResponse.TrendData(
                            month, avgDensity, avgVehicleCount, avgSpeed, monthData.size());
                })
                .sorted(Comparator.comparing(TrendAnalysisResponse.TrendData::getPeriod))
                .collect(Collectors.toList());
    }

    /**
     * Analyze weekly traffic trends
     */
    private List<TrendAnalysisResponse.TrendData> analyzeWeeklyTrends(List<TrafficData> data) {
        Map<String, List<TrafficData>> weeklyData = data.stream()
                .collect(Collectors.groupingBy(d -> 
                    d.getTimestamp().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH)));

        return weeklyData.entrySet().stream()
                .map(entry -> {
                    String dayOfWeek = entry.getKey();
                    List<TrafficData> dayData = entry.getValue();
                    
                    String avgDensity = calculateAverageTrafficDensity(dayData);
                    Double avgVehicleCount = calculateAverageVehicleCount(dayData);
                    Double avgSpeed = calculateAverageSpeed(dayData);
                    
                    return new TrendAnalysisResponse.TrendData(
                            dayOfWeek, avgDensity, avgVehicleCount, avgSpeed, dayData.size());
                })
                .collect(Collectors.toList());
    }

    /**
     * Analyze daily traffic trends (hourly breakdown)
     */
    private List<TrendAnalysisResponse.TrendData> analyzeDailyTrends(List<TrafficData> data) {
        Map<Integer, List<TrafficData>> hourlyData = data.stream()
                .collect(Collectors.groupingBy(d -> d.getTimestamp().getHour()));

        return hourlyData.entrySet().stream()
                .map(entry -> {
                    Integer hour = entry.getKey();
                    List<TrafficData> hourData = entry.getValue();
                    
                    String avgDensity = calculateAverageTrafficDensity(hourData);
                    Double avgVehicleCount = calculateAverageVehicleCount(hourData);
                    Double avgSpeed = calculateAverageSpeed(hourData);
                    
                    return new TrendAnalysisResponse.TrendData(
                            hour + ":00", avgDensity, avgVehicleCount, avgSpeed, hourData.size());
                })
                .sorted(Comparator.comparing(d -> Integer.parseInt(d.getPeriod().split(":")[0])))
                .collect(Collectors.toList());
    }

    /**
     * Calculate average traffic density
     */
    private String calculateAverageTrafficDensity(List<TrafficData> data) {
        if (data.isEmpty()) return "LOW";
        
        double avgScore = data.stream()
                .mapToDouble(d -> getDensityScore(d.getTrafficDensity()))
                .average()
                .orElse(1.0);
        
        if (avgScore >= 3.5) return "HIGH";
        else if (avgScore >= 2.5) return "MODERATE";
        else return "LOW";
    }

    /**
     * Calculate average vehicle count
     */
    private Double calculateAverageVehicleCount(List<TrafficData> data) {
        return data.stream()
                .filter(d -> d.getVehicleCount() != null)
                .mapToDouble(TrafficData::getVehicleCount)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculate average speed
     */
    private Double calculateAverageSpeed(List<TrafficData> data) {
        return data.stream()
                .filter(d -> d.getAverageSpeed() != null)
                .mapToDouble(TrafficData::getAverageSpeed)
                .average()
                .orElse(0.0);
    }

    /**
     * Generate trend summary
     */
    private TrendAnalysisResponse.TrendSummary generateTrendSummary(List<TrafficData> data) {
        // Find peak and low traffic times
        Map<Integer, List<TrafficData>> hourlyData = data.stream()
                .collect(Collectors.groupingBy(d -> d.getTimestamp().getHour()));

        String peakHour = hourlyData.entrySet().stream()
                .max(Comparator.comparing(entry -> calculateAverageTrafficScore(entry.getValue())))
                .map(entry -> entry.getKey() + ":00")
                .orElse("Unknown");

        String lowHour = hourlyData.entrySet().stream()
                .min(Comparator.comparing(entry -> calculateAverageTrafficScore(entry.getValue())))
                .map(entry -> entry.getKey() + ":00")
                .orElse("Unknown");

        // Determine overall trend
        String overallTrend = determineOverallTrend(data);
        
        // Generate recommendations
        String recommendations = generateRecommendations(data, overallTrend);

        return new TrendAnalysisResponse.TrendSummary(
                overallTrend, peakHour, lowHour, recommendations);
    }

    /**
     * Calculate average traffic score for a list of data points
     */
    private double calculateAverageTrafficScore(List<TrafficData> data) {
        return data.stream()
                .mapToDouble(d -> getDensityScore(d.getTrafficDensity()))
                .average()
                .orElse(1.0);
    }

    /**
     * Get numeric score for traffic density
     */
    private double getDensityScore(TrafficData.TrafficDensity density) {
        return switch (density) {
            case LOW -> 1.0;
            case MODERATE -> 2.0;
            case HIGH -> 3.0;
            case CRITICAL -> 4.0;
        };
    }

    /**
     * Determine overall traffic trend
     */
    private String determineOverallTrend(List<TrafficData> data) {
        if (data.size() < 2) return "INSUFFICIENT_DATA";
        
        // Compare recent data with older data
        int midPoint = data.size() / 2;
        List<TrafficData> recentData = data.subList(0, midPoint);
        List<TrafficData> olderData = data.subList(midPoint, data.size());
        
        double recentAvg = calculateAverageTrafficScore(recentData);
        double olderAvg = calculateAverageTrafficScore(olderData);
        
        if (recentAvg > olderAvg + 0.3) return "INCREASING";
        else if (recentAvg < olderAvg - 0.3) return "DECREASING";
        else return "STABLE";
    }

    /**
     * Generate recommendations based on traffic trends
     */
    private String generateRecommendations(List<TrafficData> data, String overallTrend) {
        StringBuilder recommendations = new StringBuilder();
        
        switch (overallTrend) {
            case "INCREASING":
                recommendations.append("Traffic congestion is increasing. Consider implementing additional traffic management measures. ");
                break;
            case "DECREASING":
                recommendations.append("Traffic conditions are improving. Current measures are effective. ");
                break;
            case "STABLE":
                recommendations.append("Traffic patterns are stable. Monitor for any changes. ");
                break;
        }
        
        // Add time-based recommendations
        double avgScore = calculateAverageTrafficScore(data);
        if (avgScore > 2.5) {
            recommendations.append("Consider signal optimization during peak hours. ");
        }
        
        return recommendations.toString();
    }

    /**
     * Create empty trend response for locations with no data
     */
    private TrendAnalysisResponse createEmptyTrendResponse(String location, String period) {
        TrendAnalysisResponse response = new TrendAnalysisResponse();
        response.setLocation(location);
        response.setPeriod(period);
        response.setMonthlyTrend(Collections.emptyList());
        response.setWeeklyTrend(Collections.emptyList());
        response.setDailyTrend(Collections.emptyList());
        
        TrendAnalysisResponse.TrendSummary summary = new TrendAnalysisResponse.TrendSummary(
                "NO_DATA", "Unknown", "Unknown", 
                "No historical data available for analysis. Start collecting traffic data for this location.");
        response.setSummary(summary);
        
        return response;
    }
}
