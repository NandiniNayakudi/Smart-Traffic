package com.traffic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for authentication
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private String username;
    private String message;
    
    public AuthResponse(String token, Long expiresIn, String username) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.username = username;
        this.message = "Authentication successful";
    }
    
    public AuthResponse(String message) {
        this.message = message;
    }
}
