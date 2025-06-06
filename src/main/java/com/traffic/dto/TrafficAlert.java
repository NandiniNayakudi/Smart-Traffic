package com.traffic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for real-time traffic alerts
 * Enhanced for Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrafficAlert {
    
    private String id;
    private String type;
    private String severity;
    private String location;
    private String message;
    private LocalDateTime timestamp;
    private boolean active;
    private Double latitude;
    private Double longitude;
    private String description;
    private String recommendedAction;
    
    // Alert types
    public static final String HIGH_TRAFFIC = "HIGH_TRAFFIC";
    public static final String LOW_SPEED = "LOW_SPEED";
    public static final String ACCIDENT = "ACCIDENT";
    public static final String ROAD_CLOSURE = "ROAD_CLOSURE";
    public static final String WEATHER_IMPACT = "WEATHER_IMPACT";
    public static final String SIGNAL_MALFUNCTION = "SIGNAL_MALFUNCTION";
    
    // Severity levels
    public static final String INFO = "INFO";
    public static final String WARNING = "WARNING";
    public static final String CRITICAL = "CRITICAL";
    public static final String EMERGENCY = "EMERGENCY";
}
