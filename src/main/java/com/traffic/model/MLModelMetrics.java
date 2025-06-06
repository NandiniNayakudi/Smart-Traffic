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
 * Entity for tracking Machine Learning model performance metrics
 * Part of Smart City Traffic Optimization System Using Cloud-Based AI
 * Monitors Google Cloud AI model performance and accuracy
 */
@Entity
@Table(name = "ml_model_metrics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MLModelMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Model name is required")
    @Column(name = "model_name", nullable = false)
    private String modelName;
    
    @NotBlank(message = "Model version is required")
    @Column(name = "model_version", nullable = false)
    private String modelVersion;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "model_type", nullable = false)
    private ModelType modelType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "model_status", nullable = false)
    private ModelStatus modelStatus;
    
    @Column(name = "accuracy_score")
    private Double accuracyScore;
    
    @Column(name = "precision_score")
    private Double precisionScore;
    
    @Column(name = "recall_score")
    private Double recallScore;
    
    @Column(name = "f1_score")
    private Double f1Score;
    
    @Column(name = "training_data_size")
    private Long trainingDataSize;
    
    @Column(name = "validation_data_size")
    private Long validationDataSize;
    
    @Column(name = "test_data_size")
    private Long testDataSize;
    
    @Column(name = "training_duration_minutes")
    private Integer trainingDurationMinutes;
    
    @Column(name = "predictions_per_minute")
    private Integer predictionsPerMinute;
    
    @Column(name = "google_cloud_model_id")
    private String googleCloudModelId;
    
    @Column(name = "bigquery_dataset")
    private String bigqueryDataset;
    
    @Column(name = "bigquery_table")
    private String bigqueryTable;
    
    @Column(name = "feature_count")
    private Integer featureCount;
    
    @Column(name = "hyperparameters", columnDefinition = "TEXT")
    private String hyperparameters; // JSON string
    
    @Column(name = "loss_function")
    private String lossFunction;
    
    @Column(name = "optimizer")
    private String optimizer;
    
    @Column(name = "learning_rate")
    private Double learningRate;
    
    @Column(name = "batch_size")
    private Integer batchSize;
    
    @Column(name = "epochs_completed")
    private Integer epochsCompleted;
    
    @Column(name = "total_epochs")
    private Integer totalEpochs;
    
    @Column(name = "early_stopping_patience")
    private Integer earlyStoppingPatience;
    
    @Column(name = "cross_validation_folds")
    private Integer crossValidationFolds;
    
    @Column(name = "feature_importance", columnDefinition = "TEXT")
    private String featureImportance; // JSON string
    
    @Column(name = "confusion_matrix", columnDefinition = "TEXT")
    private String confusionMatrix; // JSON string
    
    @Column(name = "roc_auc_score")
    private Double rocAucScore;
    
    @Column(name = "mean_absolute_error")
    private Double meanAbsoluteError;
    
    @Column(name = "mean_squared_error")
    private Double meanSquaredError;
    
    @Column(name = "r_squared_score")
    private Double rSquaredScore;
    
    @Column(name = "deployment_environment")
    private String deploymentEnvironment;
    
    @Column(name = "model_size_mb")
    private Double modelSizeMb;
    
    @Column(name = "inference_latency_ms")
    private Double inferenceLatencyMs;
    
    @Column(name = "memory_usage_mb")
    private Double memoryUsageMb;
    
    @Column(name = "cpu_usage_percent")
    private Double cpuUsagePercent;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(name = "training_started_at")
    private LocalDateTime trainingStartedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(name = "training_completed_at")
    private LocalDateTime trainingCompletedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    @Column(name = "last_prediction_at")
    private LocalDateTime lastPredictionAt;
    
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
     * Machine Learning model types
     */
    public enum ModelType {
        TRAFFIC_PREDICTION("Traffic flow and congestion prediction"),
        ROUTE_OPTIMIZATION("Eco-friendly route optimization"),
        SIGNAL_TIMING("Adaptive traffic signal timing"),
        DEMAND_FORECASTING("Traffic demand forecasting"),
        ANOMALY_DETECTION("Traffic anomaly and incident detection"),
        EMISSION_PREDICTION("Vehicle emission prediction"),
        WEATHER_IMPACT("Weather impact on traffic"),
        PATTERN_RECOGNITION("Traffic pattern recognition");
        
        private final String description;
        
        ModelType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Model deployment and operational status
     */
    public enum ModelStatus {
        TRAINING("Model is currently training"),
        VALIDATING("Model is being validated"),
        TESTING("Model is being tested"),
        DEPLOYED("Model is deployed and active"),
        RETIRED("Model has been retired"),
        FAILED("Model training or deployment failed"),
        UPDATING("Model is being updated"),
        MONITORING("Model is in monitoring phase");
        
        private final String description;
        
        ModelStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * Calculate overall model performance score
     */
    public Double calculateOverallPerformance() {
        if (accuracyScore == null) return 0.0;
        
        double performance = accuracyScore;
        
        // Adjust based on other metrics if available
        if (f1Score != null) {
            performance = (performance + f1Score) / 2.0;
        }
        
        if (rocAucScore != null) {
            performance = (performance + (rocAucScore * 100)) / 2.0;
        }
        
        return Math.min(100.0, performance);
    }
    
    /**
     * Check if model meets production quality standards
     */
    public boolean isProductionReady() {
        return modelStatus == ModelStatus.DEPLOYED && 
               accuracyScore != null && accuracyScore >= 85.0 &&
               inferenceLatencyMs != null && inferenceLatencyMs <= 100.0;
    }
    
    /**
     * Get training progress percentage
     */
    public Double getTrainingProgress() {
        if (epochsCompleted == null || totalEpochs == null || totalEpochs == 0) {
            return 0.0;
        }
        return (epochsCompleted.doubleValue() / totalEpochs.doubleValue()) * 100.0;
    }
    
    /**
     * Calculate model efficiency score
     */
    public Double calculateEfficiencyScore() {
        if (accuracyScore == null || inferenceLatencyMs == null) {
            return 0.0;
        }
        
        // Higher accuracy, lower latency = higher efficiency
        double latencyPenalty = Math.min(50.0, inferenceLatencyMs / 2.0);
        return Math.max(0.0, accuracyScore - latencyPenalty);
    }
}
