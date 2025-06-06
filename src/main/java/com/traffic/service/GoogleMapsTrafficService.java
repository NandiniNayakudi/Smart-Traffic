package com.traffic.service;

import com.google.maps.DirectionsApi;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.*;
import com.traffic.config.GoogleCloudConfig;
import com.traffic.model.TrafficData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Google Maps Real-Time Traffic Service
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleMapsTrafficService {

    private final GeoApiContext geoApiContext;
    private final GoogleCloudConfig.GoogleCloudProperties googleCloudProperties;

    @Autowired
    @Lazy
    private RealTimeTrafficService realTimeTrafficService;

    // Major intersection coordinates for monitoring
    private final List<TrafficLocation> monitoredLocations = Arrays.asList(
        new TrafficLocation("Times Square, New York", 40.7580, -73.9855),
        new TrafficLocation("Union Square, New York", 40.7359, -73.9911),
        new TrafficLocation("Washington Square Park, New York", 40.7308, -73.9973),
        new TrafficLocation("Brooklyn Bridge, New York", 40.7061, -73.9969),
        new TrafficLocation("Manhattan Bridge, New York", 40.7071, -73.9903),
        new TrafficLocation("Williamsburg Bridge, New York", 40.7134, -73.9634),
        new TrafficLocation("Lincoln Tunnel, New York", 40.7614, -73.9776),
        new TrafficLocation("Holland Tunnel, New York", 40.7267, -74.0134),
        new TrafficLocation("George Washington Bridge, New York", 40.8517, -73.9527),
        new TrafficLocation("Queensboro Bridge, New York", 40.7564, -73.9638)
    );

    /**
     * Get real-time traffic data from Google Maps
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void fetchRealTimeTrafficData() {
        if (geoApiContext == null) {
            log.debug("Google Maps API not configured, using simulated data");
            generateSimulatedTrafficData();
            return;
        }

        log.info("Fetching real-time traffic data from Google Maps API");
        
        for (TrafficLocation location : monitoredLocations) {
            try {
                fetchTrafficForLocation(location);
            } catch (Exception e) {
                log.error("Error fetching traffic data for {}: {}", location.getName(), e.getMessage());
            }
        }
    }

    /**
     * Fetch traffic data for a specific location
     */
    @Async
    public CompletableFuture<TrafficData> fetchTrafficForLocation(TrafficLocation location) {
        try {
            // Get traffic data using Distance Matrix API with traffic model
            DistanceMatrix matrix = DistanceMatrixApi.newRequest(geoApiContext)
                .origins(new LatLng(location.getLatitude(), location.getLongitude()))
                .destinations(getNearbyDestinations(location))
                .mode(TravelMode.DRIVING)
                .trafficModel(TrafficModel.BEST_GUESS)
                .departureTime(java.time.Instant.now())
                .await();

            TrafficData trafficData = processDistanceMatrixResponse(matrix, location);
            
            // Send to real-time processing
            if (realTimeTrafficService != null) {
                realTimeTrafficService.processRealTimeTrafficData(trafficData);
            }

            return CompletableFuture.completedFuture(trafficData);

        } catch (Exception e) {
            log.error("Error fetching traffic data for {}: {}", location.getName(), e.getMessage());
            return CompletableFuture.completedFuture(createFallbackTrafficData(location));
        }
    }

    /**
     * Get directions with real-time traffic
     */
    public DirectionsResult getDirectionsWithTraffic(String origin, String destination) {
        if (geoApiContext == null) {
            log.warn("Google Maps API not configured");
            return null;
        }

        try {
            return DirectionsApi.newRequest(geoApiContext)
                .origin(origin)
                .destination(destination)
                .mode(TravelMode.DRIVING)
                .trafficModel(TrafficModel.BEST_GUESS)
                .departureTime(java.time.Instant.now())
                .alternatives(true)
                .await();
        } catch (Exception e) {
            log.error("Error getting directions from {} to {}: {}", origin, destination, e.getMessage());
            return null;
        }
    }

    /**
     * Get real-time traffic conditions for a route
     */
    public Map<String, Object> getRouteTrafficConditions(String origin, String destination) {
        DirectionsResult directions = getDirectionsWithTraffic(origin, destination);
        
        if (directions == null || directions.routes.length == 0) {
            return createFallbackRouteData(origin, destination);
        }

        DirectionsRoute route = directions.routes[0];
        DirectionsLeg leg = route.legs[0];

        Map<String, Object> trafficConditions = new HashMap<>();
        trafficConditions.put("origin", origin);
        trafficConditions.put("destination", destination);
        trafficConditions.put("distance", leg.distance.humanReadable);
        trafficConditions.put("distanceMeters", leg.distance.inMeters);
        trafficConditions.put("duration", leg.duration.humanReadable);
        trafficConditions.put("durationSeconds", leg.duration.inSeconds);
        
        // Traffic duration (with current traffic)
        if (leg.durationInTraffic != null) {
            trafficConditions.put("durationInTraffic", leg.durationInTraffic.humanReadable);
            trafficConditions.put("durationInTrafficSeconds", leg.durationInTraffic.inSeconds);
            
            // Calculate traffic delay
            long delay = leg.durationInTraffic.inSeconds - leg.duration.inSeconds;
            trafficConditions.put("trafficDelay", delay);
            trafficConditions.put("trafficDelayMinutes", delay / 60.0);
            
            // Determine traffic level
            double trafficRatio = (double) leg.durationInTraffic.inSeconds / leg.duration.inSeconds;
            trafficConditions.put("trafficLevel", getTrafficLevel(trafficRatio));
        }

        trafficConditions.put("polyline", route.overviewPolyline.getEncodedPath());
        trafficConditions.put("timestamp", LocalDateTime.now());

        return trafficConditions;
    }

    /**
     * Process Distance Matrix API response
     */
    private TrafficData processDistanceMatrixResponse(DistanceMatrix matrix, TrafficLocation location) {
        TrafficData trafficData = new TrafficData();
        trafficData.setLocation(location.getName());
        trafficData.setLatitude(location.getLatitude());
        trafficData.setLongitude(location.getLongitude());
        trafficData.setTimestamp(LocalDateTime.now());

        if (matrix.rows.length > 0 && matrix.rows[0].elements.length > 0) {
            DistanceMatrixElement element = matrix.rows[0].elements[0];
            
            if (element.status == DistanceMatrixElementStatus.OK) {
                // Calculate average speed and traffic density
                long durationSeconds = element.duration.inSeconds;
                long trafficDurationSeconds = element.durationInTraffic != null ? 
                    element.durationInTraffic.inSeconds : durationSeconds;
                
                double distanceKm = element.distance.inMeters / 1000.0;
                double averageSpeed = (distanceKm / (trafficDurationSeconds / 3600.0));
                
                trafficData.setAverageSpeed(averageSpeed);
                
                // Determine traffic density based on speed and delay
                double trafficRatio = (double) trafficDurationSeconds / durationSeconds;
                trafficData.setTrafficDensity(determineTrafficDensity(trafficRatio, averageSpeed));
                
                // Estimate vehicle count based on traffic conditions
                trafficData.setVehicleCount(estimateVehicleCount(trafficRatio, averageSpeed));
            }
        }

        // Set weather condition (would integrate with weather API in production)
        trafficData.setWeatherCondition("CLEAR");

        return trafficData;
    }

    /**
     * Get nearby destinations for traffic analysis
     */
    private LatLng[] getNearbyDestinations(TrafficLocation location) {
        // Create a small grid around the location for traffic analysis
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        double offset = 0.01; // ~1km offset

        return new LatLng[] {
            new LatLng(lat + offset, lng),
            new LatLng(lat - offset, lng),
            new LatLng(lat, lng + offset),
            new LatLng(lat, lng - offset)
        };
    }

    /**
     * Determine traffic density based on traffic ratio and speed
     */
    private TrafficData.TrafficDensity determineTrafficDensity(double trafficRatio, double averageSpeed) {
        if (trafficRatio > 2.0 || averageSpeed < 15) {
            return TrafficData.TrafficDensity.CRITICAL;
        } else if (trafficRatio > 1.5 || averageSpeed < 25) {
            return TrafficData.TrafficDensity.HIGH;
        } else if (trafficRatio > 1.2 || averageSpeed < 40) {
            return TrafficData.TrafficDensity.MODERATE;
        } else {
            return TrafficData.TrafficDensity.LOW;
        }
    }

    /**
     * Estimate vehicle count based on traffic conditions
     */
    private Integer estimateVehicleCount(double trafficRatio, double averageSpeed) {
        // Simple estimation algorithm - would use ML in production
        int baseCount = 20;
        double multiplier = Math.max(1.0, trafficRatio * (50.0 / Math.max(averageSpeed, 10.0)));
        return (int) (baseCount * multiplier);
    }

    /**
     * Get traffic level description
     */
    private String getTrafficLevel(double trafficRatio) {
        if (trafficRatio > 2.0) return "HEAVY";
        if (trafficRatio > 1.5) return "MODERATE";
        if (trafficRatio > 1.2) return "LIGHT";
        return "FREE_FLOW";
    }

    /**
     * Create fallback traffic data when API is unavailable
     */
    private TrafficData createFallbackTrafficData(TrafficLocation location) {
        TrafficData trafficData = new TrafficData();
        trafficData.setLocation(location.getName());
        trafficData.setLatitude(location.getLatitude());
        trafficData.setLongitude(location.getLongitude());
        trafficData.setTimestamp(LocalDateTime.now());
        trafficData.setTrafficDensity(TrafficData.TrafficDensity.MODERATE);
        trafficData.setAverageSpeed(35.0 + (Math.random() * 20 - 10)); // 25-45 km/h
        trafficData.setVehicleCount(30 + (int)(Math.random() * 40)); // 30-70 vehicles
        trafficData.setWeatherCondition("CLEAR");
        return trafficData;
    }

    /**
     * Create fallback route data
     */
    private Map<String, Object> createFallbackRouteData(String origin, String destination) {
        Map<String, Object> fallbackData = new HashMap<>();
        fallbackData.put("origin", origin);
        fallbackData.put("destination", destination);
        fallbackData.put("distance", "5.2 km");
        fallbackData.put("distanceMeters", 5200);
        fallbackData.put("duration", "12 mins");
        fallbackData.put("durationSeconds", 720);
        fallbackData.put("durationInTraffic", "15 mins");
        fallbackData.put("durationInTrafficSeconds", 900);
        fallbackData.put("trafficDelay", 180);
        fallbackData.put("trafficDelayMinutes", 3.0);
        fallbackData.put("trafficLevel", "MODERATE");
        fallbackData.put("timestamp", LocalDateTime.now());
        return fallbackData;
    }

    /**
     * Generate simulated traffic data when Google Maps API is not available
     */
    private void generateSimulatedTrafficData() {
        for (TrafficLocation location : monitoredLocations.subList(0, 3)) { // Process 3 locations
            TrafficData trafficData = createFallbackTrafficData(location);
            
            if (realTimeTrafficService != null) {
                realTimeTrafficService.processRealTimeTrafficData(trafficData);
            }
        }
    }

    /**
     * Traffic Location Data Class
     */
    public static class TrafficLocation {
        private final String name;
        private final double latitude;
        private final double longitude;

        public TrafficLocation(String name, double latitude, double longitude) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getName() { return name; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
}
