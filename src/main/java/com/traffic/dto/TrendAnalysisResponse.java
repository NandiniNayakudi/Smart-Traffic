package com.traffic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for traffic trend analysis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendAnalysisResponse {
    
    private String location;
    private String period;
    private List<TrendData> monthlyTrend;
    private List<TrendData> weeklyTrend;
    private List<TrendData> dailyTrend;
    private TrendSummary summary;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private String period;
        private String avgDensity;
        private Double avgVehicleCount;
        private Double avgSpeed;
        private Integer dataPoints;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendSummary {
        private String overallTrend;
        private String peakTrafficTime;
        private String lowTrafficTime;
        private String recommendations;
    }
}
