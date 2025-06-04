package com.traffic.dto;

import com.traffic.model.TrafficData.TrafficDensity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for traffic prediction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {
    
    private TrafficDensity prediction;
    private Double confidence;
    private String message;
    
    public PredictionResponse(TrafficDensity prediction, Double confidence) {
        this.prediction = prediction;
        this.confidence = confidence;
        this.message = String.format("Traffic prediction: %s with %.2f%% confidence", 
                                    prediction.name(), confidence * 100);
    }
}
