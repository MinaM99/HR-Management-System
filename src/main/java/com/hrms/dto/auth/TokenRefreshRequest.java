package com.hrms.dto.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object for refresh token requests.
 * 
 * This DTO is used when clients need to obtain a new access token
 * using their refresh token. This is part of the JWT refresh token
 * flow that allows clients to maintain authentication without
 * requiring users to re-login.
 * 
 * Security considerations:
 * - Refresh tokens should have longer expiration than access tokens
 * - Validate refresh token before issuing new access token
 * - Consider implementing refresh token rotation for enhanced security
 * - Log refresh token usage for security monitoring
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
public class TokenRefreshRequest {
    
    /**
     * The refresh token obtained during initial authentication.
     */
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
    
    // Constructors
    
    /**
     * Default constructor for JSON deserialization.
     */
    public TokenRefreshRequest() {
    }
    
    /**
     * Constructor with refresh token.
     *
     * @param refreshToken the refresh token
     */
    public TokenRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Getters and Setters
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    @Override
    public String toString() {
        return "TokenRefreshRequest{" +
                "refreshToken='[PROTECTED]'" + // Don't expose actual token in logs
                '}';
    }
}