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
 * Entity representing AI-powered traffic predictions
 * Part of Smart City Traffic Optimization System Using Cloud-Based AI
 * Research by: Ballaram Krishna Chaithanya, Nandini Nayakudi, Siddharth Vinayak Bahl, Meenakshi
 * Department of Computer Science and Engineering, Amrita School of Engineering, Amritapuri, Kerala
 */
@Entity
@Table(name = "traffic_predictions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrafficPrediction {
    
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
    
    @NotNull(message = "Predicted traffic density is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "predicted_density", nullable = false)
    private TrafficData.TrafficDensity predictedDensity;
    
    @NotNull(message = "Prediction timestamp is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(name = "prediction_time", nullable = false)
    private LocalDateTime predictionTime;
    
    @NotNull(message = "Prediction for timestamp is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(name = "predicted_for_time", nullable = false)
    private LocalDateTime predictedForTime;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "predicted_vehicle_count")
    private Integer predictedVehicleCount;
    
    @Column(name = "predicted_average_speed")
    private Double predictedAverageSpeed;
    
    @Column(name = "congestion_probability")
    private Double congestionProbability;
    
    @Column(name = "model_version")
    private String modelVersion;
    
    @Column(name = "ml_algorithm")
    private String mlAlgorithm;
    
    @Column(name = "google_cloud_job_id")
    private String googleCloudJobId;
    
    @Column(name = "bigquery_dataset")
    private String bigqueryDataset;
    
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
     * Calculate prediction accuracy based on actual vs predicted
     */
    public Double calculateAccuracy(TrafficData.TrafficDensity actualDensity) {
        if (actualDensity == null || predictedDensity == null) {
            return 0.0;
        }
        
        // Simple accuracy calculation - can be enhanced with more sophisticated metrics
        if (actualDensity == predictedDensity) {
            return 100.0;
        } else if (Math.abs(actualDensity.ordinal() - predictedDensity.ordinal()) == 1) {
            return 75.0; // Close prediction
        } else {
            return 25.0; // Poor prediction
        }
    }
    
    /**
     * Check if this is a high-confidence prediction
     */
    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore >= 0.8;
    }
    
    /**
     * Get prediction horizon in minutes
     */
    public long getPredictionHorizonMinutes() {
        if (predictionTime != null && predictedForTime != null) {
            return java.time.Duration.between(predictionTime, predictedForTime).toMinutes();
        }
        return 0;
    }
}
