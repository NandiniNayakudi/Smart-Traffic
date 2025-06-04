package com.traffic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for traffic signal optimization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalOptimizationRequest {
    
    @NotBlank(message = "Intersection ID is required")
    private String intersectionId;
    
    @NotNull(message = "North traffic count is required")
    @Min(value = 0, message = "Traffic count cannot be negative")
    private Integer north;
    
    @NotNull(message = "South traffic count is required")
    @Min(value = 0, message = "Traffic count cannot be negative")
    private Integer south;
    
    @NotNull(message = "East traffic count is required")
    @Min(value = 0, message = "Traffic count cannot be negative")
    private Integer east;
    
    @NotNull(message = "West traffic count is required")
    @Min(value = 0, message = "Traffic count cannot be negative")
    private Integer west;
    
    private String timeOfDay;
    private String dayOfWeek;
    private String weatherCondition;
}
