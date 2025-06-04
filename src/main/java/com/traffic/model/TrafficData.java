package com.traffic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Entity representing real-time traffic data
 */
@Entity
@Table(name = "traffic_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrafficData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Location is required")
    @Column(nullable = false)
    private String location;
    
    @NotNull(message = "Latitude is required")
    @Column(nullable = false)
    private Double latitude;
    
    @NotNull(message = "Longitude is required")
    @Column(nullable = false)
    private Double longitude;
    
    @NotNull(message = "Traffic density is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "traffic_density", nullable = false)
    private TrafficDensity trafficDensity;
    
    @NotNull(message = "Timestamp is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "vehicle_count")
    private Integer vehicleCount;
    
    @Column(name = "average_speed")
    private Double averageSpeed;
    
    @Column(name = "weather_condition")
    private String weatherCondition;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Traffic density levels
     */
    public enum TrafficDensity {
        LOW("Low traffic density"),
        MODERATE("Moderate traffic density"),
        HIGH("High traffic density"),
        CRITICAL("Critical traffic congestion");
        
        private final String description;
        
        TrafficDensity(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
