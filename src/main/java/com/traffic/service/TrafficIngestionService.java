package com.traffic.service;

import com.traffic.model.TrafficData;
import com.traffic.repository.TrafficDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for handling real-time traffic data ingestion
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TrafficIngestionService {

    private final TrafficDataRepository trafficDataRepository;

    /**
     * Ingest real-time traffic data
     */
    public TrafficData ingestTrafficData(TrafficData trafficData) {
        try {
            // Set timestamp if not provided
            if (trafficData.getTimestamp() == null) {
                trafficData.setTimestamp(LocalDateTime.now());
            }

            // Validate and enrich data
            validateTrafficData(trafficData);
            enrichTrafficData(trafficData);

            // Save to database
            TrafficData savedData = trafficDataRepository.save(trafficData);
            
            log.info("Successfully ingested traffic data for location: {} with density: {}", 
                    savedData.getLocation(), savedData.getTrafficDensity());
            
            return savedData;
            
        } catch (Exception e) {
            log.error("Error ingesting traffic data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ingest traffic data", e);
        }
    }

    /**
     * Get traffic data for a location
     */
    @Transactional(readOnly = true)
    public List<TrafficData> getTrafficData(String location, Integer limit) {
        try {
            if (location != null && !location.trim().isEmpty()) {
                return trafficDataRepository.findByLocationContainingIgnoreCaseOrderByTimestampDesc(
                        location.trim(), org.springframework.data.domain.PageRequest.of(0, limit));
            } else {
                return trafficDataRepository.findAllByOrderByTimestampDesc(
                        org.springframework.data.domain.PageRequest.of(0, limit));
            }
        } catch (Exception e) {
            log.error("Error retrieving traffic data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve traffic data", e);
        }
    }

    /**
     * Get recent traffic data for a specific location and coordinates
     */
    @Transactional(readOnly = true)
    public List<TrafficData> getRecentTrafficData(Double latitude, Double longitude, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return trafficDataRepository.findByLatitudeBetweenAndLongitudeBetweenAndTimestampAfter(
                latitude - 0.01, latitude + 0.01,
                longitude - 0.01, longitude + 0.01,
                since);
    }

    /**
     * Batch ingest multiple traffic data points
     */
    public List<TrafficData> batchIngestTrafficData(List<TrafficData> trafficDataList) {
        try {
            log.info("Batch ingesting {} traffic data points", trafficDataList.size());
            
            // Validate and enrich each data point
            trafficDataList.forEach(data -> {
                if (data.getTimestamp() == null) {
                    data.setTimestamp(LocalDateTime.now());
                }
                validateTrafficData(data);
                enrichTrafficData(data);
            });

            // Save all data points
            List<TrafficData> savedData = trafficDataRepository.saveAll(trafficDataList);
            
            log.info("Successfully batch ingested {} traffic data points", savedData.size());
            return savedData;
            
        } catch (Exception e) {
            log.error("Error batch ingesting traffic data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to batch ingest traffic data", e);
        }
    }

    /**
     * Validate traffic data
     */
    private void validateTrafficData(TrafficData trafficData) {
        if (trafficData.getLatitude() == null || trafficData.getLongitude() == null) {
            throw new IllegalArgumentException("Latitude and longitude are required");
        }
        
        if (trafficData.getLatitude() < -90 || trafficData.getLatitude() > 90) {
            throw new IllegalArgumentException("Invalid latitude value");
        }
        
        if (trafficData.getLongitude() < -180 || trafficData.getLongitude() > 180) {
            throw new IllegalArgumentException("Invalid longitude value");
        }
        
        if (trafficData.getLocation() == null || trafficData.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required");
        }
        
        if (trafficData.getTrafficDensity() == null) {
            throw new IllegalArgumentException("Traffic density is required");
        }
    }

    /**
     * Enrich traffic data with additional information
     */
    private void enrichTrafficData(TrafficData trafficData) {
        // Set default values if not provided
        if (trafficData.getVehicleCount() == null) {
            trafficData.setVehicleCount(estimateVehicleCount(trafficData.getTrafficDensity()));
        }
        
        if (trafficData.getAverageSpeed() == null) {
            trafficData.setAverageSpeed(estimateAverageSpeed(trafficData.getTrafficDensity()));
        }
        
        // Set weather condition if not provided (could integrate with weather API)
        if (trafficData.getWeatherCondition() == null) {
            trafficData.setWeatherCondition("CLEAR");
        }
    }

    /**
     * Estimate vehicle count based on traffic density
     */
    private Integer estimateVehicleCount(TrafficData.TrafficDensity density) {
        return switch (density) {
            case LOW -> 15;
            case MODERATE -> 35;
            case HIGH -> 65;
            case CRITICAL -> 100;
        };
    }

    /**
     * Estimate average speed based on traffic density
     */
    private Double estimateAverageSpeed(TrafficData.TrafficDensity density) {
        return switch (density) {
            case LOW -> 45.0;
            case MODERATE -> 25.0;
            case HIGH -> 15.0;
            case CRITICAL -> 5.0;
        };
    }
}
