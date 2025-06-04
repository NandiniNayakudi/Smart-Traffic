package com.traffic.service;

import com.traffic.dto.AuthRequest;
import com.traffic.dto.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        
        // Set private fields using reflection
        ReflectionTestUtils.setField(authService, "jwtSecret", "testSecretKey123456789012345678901234567890");
        ReflectionTestUtils.setField(authService, "jwtExpiration", 3600000L); // 1 hour
    }

    @Test
    void testAuthenticate_ValidCredentials() {
        // Given
        AuthRequest request = new AuthRequest("admin", "secure123");

        // When
        AuthResponse response = authService.authenticate(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("admin", response.getUsername());
        assertEquals(3600000L, response.getExpiresIn());
        assertEquals("Authentication successful", response.getMessage());
    }

    @Test
    void testAuthenticate_InvalidCredentials() {
        // Given
        AuthRequest request = new AuthRequest("admin", "wrongpassword");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate(request);
        });
    }

    @Test
    void testAuthenticate_NonExistentUser() {
        // Given
        AuthRequest request = new AuthRequest("nonexistent", "password");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.authenticate(request);
        });
    }

    @Test
    void testValidateToken_ValidToken() {
        // Given
        AuthRequest request = new AuthRequest("admin", "secure123");
        AuthResponse authResponse = authService.authenticate(request);
        String token = authResponse.getToken();

        // When
        AuthResponse validationResponse = authService.validateToken(token);

        // Then
        assertNotNull(validationResponse);
        assertTrue(validationResponse.getMessage().contains("Token is valid"));
    }

    @Test
    void testValidateToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        AuthResponse response = authService.validateToken(invalidToken);

        // Then
        assertNotNull(response);
        assertEquals("Invalid token", response.getMessage());
    }

    @Test
    void testRefreshToken_ValidToken() {
        // Given
        AuthRequest request = new AuthRequest("user", "password123");
        AuthResponse authResponse = authService.authenticate(request);
        String originalToken = authResponse.getToken();

        // When
        AuthResponse refreshResponse = authService.refreshToken(originalToken);

        // Then
        assertNotNull(refreshResponse);
        assertNotNull(refreshResponse.getToken());
        assertNotEquals(originalToken, refreshResponse.getToken());
        assertEquals("user", refreshResponse.getUsername());
    }

    @Test
    void testRefreshToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(invalidToken);
        });
    }

    @Test
    void testLogout_ValidToken() {
        // Given
        AuthRequest request = new AuthRequest("admin", "secure123");
        AuthResponse authResponse = authService.authenticate(request);
        String token = authResponse.getToken();

        // When
        authService.logout(token);

        // Then
        // Token should be blacklisted
        AuthResponse validationResponse = authService.validateToken(token);
        assertEquals("Token has been invalidated", validationResponse.getMessage());
    }

    @Test
    void testLogout_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        // Should not throw exception for logout
        assertDoesNotThrow(() -> {
            authService.logout(invalidToken);
        });
    }

    @Test
    void testGetUsernameFromToken_ValidToken() {
        // Given
        AuthRequest request = new AuthRequest("traffic_manager", "traffic2024");
        AuthResponse authResponse = authService.authenticate(request);
        String token = authResponse.getToken();

        // When
        String username = authService.getUsernameFromToken(token);

        // Then
        assertEquals("traffic_manager", username);
    }

    @Test
    void testGetUsernameFromToken_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        String username = authService.getUsernameFromToken(invalidToken);

        // Then
        assertNull(username);
    }

    @Test
    void testIsTokenValid_ValidToken() {
        // Given
        AuthRequest request = new AuthRequest("admin", "secure123");
        AuthResponse authResponse = authService.authenticate(request);
        String token = authResponse.getToken();

        // When
        boolean isValid = authService.isTokenValid(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testIsTokenValid_InvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = authService.isTokenValid(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testIsTokenValid_BlacklistedToken() {
        // Given
        AuthRequest request = new AuthRequest("admin", "secure123");
        AuthResponse authResponse = authService.authenticate(request);
        String token = authResponse.getToken();
        
        // Logout to blacklist the token
        authService.logout(token);

        // When
        boolean isValid = authService.isTokenValid(token);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testAuthenticate_AllValidUsers() {
        // Test all predefined users
        String[][] users = {
            {"admin", "secure123"},
            {"user", "password123"},
            {"traffic_manager", "traffic2024"}
        };

        for (String[] user : users) {
            AuthRequest request = new AuthRequest(user[0], user[1]);
            AuthResponse response = authService.authenticate(request);
            
            assertNotNull(response);
            assertNotNull(response.getToken());
            assertEquals(user[0], response.getUsername());
        }
    }

    @Test
    void testTokenExpiration() throws InterruptedException {
        // Given - Set very short expiration for testing
        ReflectionTestUtils.setField(authService, "jwtExpiration", 1000L); // 1 second
        
        AuthRequest request = new AuthRequest("admin", "secure123");
        AuthResponse authResponse = authService.authenticate(request);
        String token = authResponse.getToken();

        // When - Wait for token to expire
        Thread.sleep(1500); // Wait 1.5 seconds

        // Then - Token should be invalid due to expiration
        boolean isValid = authService.isTokenValid(token);
        assertFalse(isValid);
    }

    @Test
    void testRefreshToken_BlacklistedOriginalToken() {
        // Given
        AuthRequest request = new AuthRequest("admin", "secure123");
        AuthResponse authResponse = authService.authenticate(request);
        String originalToken = authResponse.getToken();
        
        // Refresh the token
        AuthResponse refreshResponse = authService.refreshToken(originalToken);
        String newToken = refreshResponse.getToken();

        // When - Try to use original token
        boolean originalTokenValid = authService.isTokenValid(originalToken);
        boolean newTokenValid = authService.isTokenValid(newToken);

        // Then
        assertFalse(originalTokenValid); // Original token should be blacklisted
        assertTrue(newTokenValid); // New token should be valid
    }
}
