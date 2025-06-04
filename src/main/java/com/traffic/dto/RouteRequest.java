package com.traffic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for route recommendation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteRequest {
    
    @NotBlank(message = "Source is required")
    private String source;
    
    @NotBlank(message = "Destination is required")
    private String destination;
    
    private Boolean eco = true; // Default to eco-friendly route
    private String travelMode = "DRIVING"; // DRIVING, WALKING, BICYCLING, TRANSIT
    private Boolean avoidTolls = false;
    private Boolean avoidHighways = false;
}
