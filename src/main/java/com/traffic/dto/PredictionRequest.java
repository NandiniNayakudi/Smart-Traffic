package com.traffic.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * Request DTO for traffic prediction
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {
    
    @NotNull(message = "Latitude is required")
    private Double lat;
    
    @NotNull(message = "Longitude is required")
    private Double lon;
    
    @NotNull(message = "Timestamp is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;
    
    private String weatherCondition;
    private String dayOfWeek;
    private Integer hour;
}
