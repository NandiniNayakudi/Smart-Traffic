package com.traffic.service;

import com.traffic.dto.AuthRequest;
import com.traffic.dto.AuthResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling authentication and JWT token management
 */
@Service
@Slf4j
public class AuthService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    // Demo users - In production, this would be from a database
    private final Map<String, String> users = Map.of(
            "admin", passwordEncoder.encode("secure123"),
            "user", passwordEncoder.encode("password123"),
            "traffic_manager", passwordEncoder.encode("traffic2024")
    );

    /**
     * Authenticate user and generate JWT token
     */
    public AuthResponse authenticate(AuthRequest authRequest) {
        try {
            String username = authRequest.getUsername();
            String password = authRequest.getPassword();

            // Validate credentials
            if (!isValidUser(username, password)) {
                throw new BadCredentialsException("Invalid username or password");
            }

            // Generate JWT token
            String token = generateToken(username);
            
            log.info("Authentication successful for user: {}", username);
            return new AuthResponse(token, jwtExpiration, username);

        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            throw new RuntimeException("Authentication failed", e);
        }
    }

    /**
     * Validate JWT token
     */
    public AuthResponse validateToken(String token) {
        try {
            if (blacklistedTokens.contains(token)) {
                return new AuthResponse("Token has been invalidated");
            }

            Claims claims = parseToken(token);
            String username = claims.getSubject();
            
            if (isTokenExpired(claims)) {
                return new AuthResponse("Token has expired");
            }

            return new AuthResponse("Token is valid for user: " + username);

        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return new AuthResponse("Invalid token");
        }
    }

    /**
     * Refresh JWT token
     */
    public AuthResponse refreshToken(String token) {
        try {
            if (blacklistedTokens.contains(token)) {
                throw new RuntimeException("Token has been invalidated");
            }

            Claims claims = parseToken(token);
            String username = claims.getSubject();

            // Generate new token
            String newToken = generateToken(username);
            
            // Blacklist old token
            blacklistedTokens.add(token);

            log.info("Token refreshed for user: {}", username);
            return new AuthResponse(newToken, jwtExpiration, username);

        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new RuntimeException("Token refresh failed", e);
        }
    }

    /**
     * Logout user and blacklist token
     */
    public void logout(String token) {
        try {
            Claims claims = parseToken(token);
            String username = claims.getSubject();
            
            blacklistedTokens.add(token);
            
            log.info("User logged out: {}", username);

        } catch (Exception e) {
            log.error("Logout failed: {}", e.getMessage());
            // Don't throw exception for logout failures
        }
    }

    /**
     * Generate JWT token for user
     */
    private String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("role", getUserRole(username));
        claims.put("tokenId", System.currentTimeMillis()); // Add unique token ID

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * Parse JWT token and extract claims
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * Validate user credentials
     */
    private boolean isValidUser(String username, String password) {
        String storedPassword = users.get(username);
        return storedPassword != null && passwordEncoder.matches(password, storedPassword);
    }

    /**
     * Get user role (simplified for demo)
     */
    private String getUserRole(String username) {
        return switch (username) {
            case "admin" -> "ADMIN";
            case "traffic_manager" -> "TRAFFIC_MANAGER";
            default -> "USER";
        };
    }

    /**
     * Extract username from token
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if token is valid and not blacklisted
     */
    public boolean isTokenValid(String token) {
        try {
            if (blacklistedTokens.contains(token)) {
                return false;
            }
            
            Claims claims = parseToken(token);
            return !isTokenExpired(claims);
            
        } catch (Exception e) {
            return false;
        }
    }
}
