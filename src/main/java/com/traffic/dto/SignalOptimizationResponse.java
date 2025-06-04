package com.traffic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for traffic signal optimization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalOptimizationResponse {
    
    private SignalTimings signalTimings;
    private String optimizationStrategy;
    private Double efficiencyImprovement;
    private String message;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignalTimings {
        private Integer north;
        private Integer south;
        private Integer east;
        private Integer west;
    }
    
    public SignalOptimizationResponse(SignalTimings signalTimings) {
        this.signalTimings = signalTimings;
        this.message = "Signal timings optimized successfully";
    }
}
