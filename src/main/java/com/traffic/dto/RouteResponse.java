package com.traffic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for route recommendation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class  RouteResponse {
    
    private List<String> recommendedRoute;
    private String estimatedTime;
    private String carbonSaved;
    private Double distanceKm;
    private String trafficCondition;
    private List<String> alternativeRoutes;
    
    public RouteResponse(List<String> recommendedRoute, String estimatedTime, String carbonSaved) {
        this.recommendedRoute = recommendedRoute;
        this.estimatedTime = estimatedTime;
        this.carbonSaved = carbonSaved;
    }
}
