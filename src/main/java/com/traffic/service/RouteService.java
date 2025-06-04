package com.traffic.service;

import com.traffic.dto.RouteRequest;
import com.traffic.dto.RouteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Service for route optimization and recommendations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {

    @Value("${google.maps.api-key}")
    private String googleMapsApiKey;

    private final Random random = new Random();

    /**
     * Get optimal route recommendation
     */
    public RouteResponse getOptimalRoute(RouteRequest request) {
        try {
            log.info("Getting optimal route from {} to {} (eco: {})", 
                    request.getSource(), request.getDestination(), request.getEco());

            // In a real implementation, this would integrate with Google Maps API
            // For now, we'll simulate route calculation
            
            if (isGoogleMapsApiAvailable()) {
                return getRouteFromGoogleMaps(request);
            } else {
                return getSimulatedRoute(request);
            }

        } catch (Exception e) {
            log.error("Error getting route recommendation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get route recommendation", e);
        }
    }

    /**
     * Get route from Google Maps API (simulated)
     */
    private RouteResponse getRouteFromGoogleMaps(RouteRequest request) {
        log.info("Using Google Maps API for route calculation");
        
        // Simulate Google Maps API call
        // In real implementation, use Google Maps Directions API
        
        return createRouteResponse(request, true);
    }

    /**
     * Get simulated route for demo purposes
     */
    private RouteResponse getSimulatedRoute(RouteRequest request) {
        log.info("Using simulated route calculation");
        
        return createRouteResponse(request, false);
    }

    /**
     * Create route response based on request
     */
    private RouteResponse createRouteResponse(RouteRequest request, boolean isRealApi) {
        // Simulate route calculation based on source and destination
        List<String> route = generateRoute(request.getSource(), request.getDestination());
        String estimatedTime = calculateEstimatedTime(route, request.getEco());
        String carbonSaved = calculateCarbonSavings(request.getEco(), route.size());
        Double distanceKm = calculateDistance(route);
        String trafficCondition = getTrafficCondition();
        List<String> alternativeRoutes = generateAlternativeRoutes(request);

        RouteResponse response = new RouteResponse();
        response.setRecommendedRoute(route);
        response.setEstimatedTime(estimatedTime);
        response.setCarbonSaved(carbonSaved);
        response.setDistanceKm(distanceKm);
        response.setTrafficCondition(trafficCondition);
        response.setAlternativeRoutes(alternativeRoutes);

        return response;
    }

    /**
     * Generate route waypoints
     */
    private List<String> generateRoute(String source, String destination) {
        // Simulate route generation based on common Indian city routes
        if (source.toLowerCase().contains("vijayawada") || destination.toLowerCase().contains("vijayawada")) {
            return Arrays.asList(
                "NH65",
                "Benz Circle",
                "Ramavarappadu Junction",
                "PNBS Bus Stand"
            );
        } else if (source.toLowerCase().contains("bangalore") || destination.toLowerCase().contains("bangalore")) {
            return Arrays.asList(
                "Outer Ring Road",
                "Electronic City",
                "Silk Board Junction",
                "Koramangala"
            );
        } else if (source.toLowerCase().contains("hyderabad") || destination.toLowerCase().contains("hyderabad")) {
            return Arrays.asList(
                "Outer Ring Road",
                "Gachibowli",
                "HITEC City",
                "Madhapur"
            );
        } else {
            // Generic route
            return Arrays.asList(
                "Main Road",
                "City Center",
                "Highway Junction",
                destination
            );
        }
    }

    /**
     * Calculate estimated travel time
     */
    private String calculateEstimatedTime(List<String> route, Boolean isEco) {
        int baseMinutes = route.size() * 3; // 3 minutes per waypoint
        
        if (Boolean.TRUE.equals(isEco)) {
            baseMinutes += 2; // Eco routes might take slightly longer
        }
        
        // Add some randomness
        baseMinutes += random.nextInt(5);
        
        return baseMinutes + " mins";
    }

    /**
     * Calculate carbon savings for eco-friendly routes
     */
    private String calculateCarbonSavings(Boolean isEco, int routeComplexity) {
        if (!Boolean.TRUE.equals(isEco)) {
            return "0.00 kg CO₂";
        }
        
        // Simulate carbon savings calculation
        double savings = 0.05 + (routeComplexity * 0.03) + (random.nextDouble() * 0.1);
        return String.format("%.2f kg CO₂", savings);
    }

    /**
     * Calculate route distance
     */
    private Double calculateDistance(List<String> route) {
        // Simulate distance calculation (2-5 km per waypoint)
        double distance = route.size() * (2.0 + random.nextDouble() * 3.0);
        return Math.round(distance * 100.0) / 100.0;
    }

    /**
     * Get current traffic condition
     */
    private String getTrafficCondition() {
        String[] conditions = {"LIGHT", "MODERATE", "HEAVY", "CONGESTED"};
        return conditions[random.nextInt(conditions.length)];
    }

    /**
     * Generate alternative routes
     */
    private List<String> generateAlternativeRoutes(RouteRequest request) {
        return Arrays.asList(
            "Alternative Route 1: Via Express Highway",
            "Alternative Route 2: Via City Center",
            "Alternative Route 3: Via Bypass Road"
        );
    }

    /**
     * Check if Google Maps API is available
     */
    private boolean isGoogleMapsApiAvailable() {
        return googleMapsApiKey != null && 
               !googleMapsApiKey.isEmpty() && 
               !googleMapsApiKey.equals("your-api-key-here");
    }

    /**
     * Get route with traffic optimization
     */
    public RouteResponse getTrafficOptimizedRoute(RouteRequest request) {
        log.info("Getting traffic-optimized route");
        
        RouteResponse baseRoute = getOptimalRoute(request);
        
        // Apply traffic optimization
        optimizeForTraffic(baseRoute);
        
        return baseRoute;
    }

    /**
     * Optimize route based on current traffic conditions
     */
    private void optimizeForTraffic(RouteResponse route) {
        // Simulate traffic optimization
        if ("HEAVY".equals(route.getTrafficCondition()) || "CONGESTED".equals(route.getTrafficCondition())) {
            // Increase estimated time for heavy traffic
            String currentTime = route.getEstimatedTime();
            int minutes = Integer.parseInt(currentTime.replaceAll("[^0-9]", ""));
            route.setEstimatedTime((minutes + 5) + " mins");
            
            // Suggest alternative route
            route.getAlternativeRoutes().add(0, "Recommended: Avoid main roads due to heavy traffic");
        }
    }
}
