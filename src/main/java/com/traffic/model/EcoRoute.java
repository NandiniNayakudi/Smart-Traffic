package com.traffic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing eco-friendly route recommendations
 * Part of Smart City Traffic Optimization System Using Cloud-Based AI
 * Focuses on sustainable urban mobility and carbon emission reduction
 */
@Entity
@Table(name = "eco_routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcoRoute {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Route name is required")
    @Column(nullable = false)
    private String routeName;
    
    @NotBlank(message = "Origin is required")
    @Column(nullable = false)
    private String origin;
    
    @NotBlank(message = "Destination is required")
    @Column(nullable = false)
    private String destination;
    
    @NotNull(message = "Origin latitude is required")
    @Column(name = "origin_lat", nullable = false)
    private Double originLat;
    
    @NotNull(message = "Origin longitude is required")
    @Column(name = "origin_lng", nullable = false)
    private Double originLng;
    
    @NotNull(message = "Destination latitude is required")
    @Column(name = "destination_lat", nullable = false)
    private Double destinationLat;
    
    @NotNull(message = "Destination longitude is required")
    @Column(name = "destination_lng", nullable = false)
    private Double destinationLng;
    
    @Column(name = "route_polyline", columnDefinition = "TEXT")
    private String routePolyline;
    
    @Column(name = "distance_km")
    private Double distanceKm;
    
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    
    @Column(name = "co2_emissions_kg")
    private Double co2EmissionsKg;
    
    @Column(name = "fuel_consumption_liters")
    private Double fuelConsumptionLiters;
    
    @Column(name = "eco_score")
    private Double ecoScore; // 0-100, higher is more eco-friendly
    
    @Column(name = "traffic_efficiency_score")
    private Double trafficEfficiencyScore;
    
    @Column(name = "air_quality_impact")
    private Double airQualityImpact;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "route_type")
    private RouteType routeType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "optimization_priority")
    private OptimizationPriority optimizationPriority;
    
    @Column(name = "google_maps_route_id")
    private String googleMapsRouteId;
    
    @Column(name = "alternative_routes_count")
    private Integer alternativeRoutesCount;
    
    @Column(name = "real_time_traffic_factor")
    private Double realTimeTrafficFactor;
    
    @Column(name = "weather_impact_factor")
    private Double weatherImpactFactor;
    
    @Column(name = "sdg11_contribution_score")
    private Double sdg11ContributionScore; // Sustainable Development Goal 11
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Route types for different optimization strategies
     */
    public enum RouteType {
        ECO_OPTIMAL("Eco-optimized route with minimal environmental impact"),
        TRAFFIC_OPTIMAL("Traffic-optimized route for fastest travel time"),
        BALANCED("Balanced route considering both eco and traffic factors"),
        SCENIC("Scenic route with environmental considerations"),
        PUBLIC_TRANSPORT_FRIENDLY("Route optimized for public transport integration");
        
        private final String description;
        
        RouteType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Optimization priorities for route calculation
     */
    public enum OptimizationPriority {
        MINIMIZE_EMISSIONS("Minimize CO2 emissions and environmental impact"),
        MINIMIZE_TIME("Minimize travel time"),
        MINIMIZE_FUEL("Minimize fuel consumption"),
        MAXIMIZE_AIR_QUALITY("Maximize air quality preservation"),
        BALANCE_ALL("Balance all factors for optimal sustainability");
        
        private final String description;
        
        OptimizationPriority(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Calculate overall sustainability score
     */
    public Double calculateSustainabilityScore() {
        if (ecoScore == null || trafficEfficiencyScore == null) {
            return 0.0;
        }
        
        double sustainability = (ecoScore * 0.6) + (trafficEfficiencyScore * 0.4);
        
        // Bonus for low emissions
        if (co2EmissionsKg != null && co2EmissionsKg < 2.0) {
            sustainability += 5.0;
        }
        
        // Bonus for air quality impact
        if (airQualityImpact != null && airQualityImpact > 0.8) {
            sustainability += 3.0;
        }
        
        return Math.min(100.0, sustainability);
    }
    
    /**
     * Check if this route contributes to SDG 11 (Sustainable Cities)
     */
    public boolean contributesToSDG11() {
        return sdg11ContributionScore != null && sdg11ContributionScore >= 70.0;
    }
    
    /**
     * Get environmental impact category
     */
    public String getEnvironmentalImpactCategory() {
        if (ecoScore == null) return "UNKNOWN";
        
        if (ecoScore >= 80) return "EXCELLENT";
        else if (ecoScore >= 60) return "GOOD";
        else if (ecoScore >= 40) return "MODERATE";
        else return "POOR";
    }
    
    /**
     * Calculate CO2 savings compared to average route
     */
    public Double calculateCO2Savings(Double averageEmissions) {
        if (co2EmissionsKg == null || averageEmissions == null) {
            return 0.0;
        }
        return Math.max(0.0, averageEmissions - co2EmissionsKg);
    }
}
