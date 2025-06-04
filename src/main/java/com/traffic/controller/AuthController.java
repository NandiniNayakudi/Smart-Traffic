package com.traffic.controller;

import com.traffic.dto.AuthRequest;
import com.traffic.dto.AuthResponse;
import com.traffic.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
public class AuthController {

    private final AuthService authService;

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        log.info("Login attempt for username: {}", authRequest.getUsername());
        
        AuthResponse authResponse = authService.authenticate(authRequest);
        
        log.info("Login successful for username: {}", authRequest.getUsername());
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Token validation endpoint
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate token", description = "Validate JWT token")
    public ResponseEntity<AuthResponse> validateToken(@RequestParam String token) {
        log.info("Token validation request");
        
        AuthResponse response = authService.validateToken(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh JWT token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String token) {
        log.info("Token refresh request");
        
        AuthResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout endpoint
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate token")
    public ResponseEntity<String> logout(@RequestParam String token) {
        log.info("Logout request");
        
        authService.logout(token);
        return ResponseEntity.ok("Logout successful");
    }
}
