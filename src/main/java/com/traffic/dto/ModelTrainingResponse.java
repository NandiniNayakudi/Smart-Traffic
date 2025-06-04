package com.traffic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for ML model training
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelTrainingResponse {
    
    private String status;
    private String modelId;
    private String message;
    private String trainingStartTime;
    private String estimatedCompletionTime;
    private Integer dataPointsUsed;
    
    public ModelTrainingResponse(String status, String modelId, String message) {
        this.status = status;
        this.modelId = modelId;
        this.message = message;
    }
}
