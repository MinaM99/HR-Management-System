package com.hrms.security.jwt;

import com.hrms.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Token Utility class for handling JWT operations.
 * 
 * This utility class provides methods for:
 * - Generating JWT tokens for authenticated users
 * - Validating JWT tokens
 * - Extracting user information from tokens
 * - Handling token expiration and security
 * 
 * Security Features:
 * - Uses HMAC-SHA256 algorithm for signing
 * - Configurable token expiration
 * - Comprehensive token validation
 * - Secure key generation and management
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
@Component
public class JwtUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    
    /**
     * JWT secret key from application properties.
     * Should be a strong, randomly generated string in production.
     */
    @Value("${hrms.app.jwtSecret}")
    private String jwtSecret;
    
    /**
     * JWT token expiration time in milliseconds.
     * Default: 24 hours (86400000 ms)
     */
    @Value("${hrms.app.jwtExpirationMs}")
    private int jwtExpirationMs;
    
    /**
     * Refresh token expiration time in milliseconds.
     * Default: 7 days (604800000 ms)
     */
    @Value("${hrms.app.jwtRefreshExpirationMs}")
    private int jwtRefreshExpirationMs;
    
    /**
     * Generates a JWT token for an authenticated user.
     * 
     * The token contains:
     * - Subject: username
     * - Issued at: current timestamp
     * - Expiration: current timestamp + configured expiration time
     * - Signature: HMAC-SHA256 with secret key
     * 
     * @param authentication the authentication object containing user details
     * @return JWT token string
     */
    public String generateJwtToken(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        // Convert roles to role names
        List<String> roleNames = userPrincipal.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());
        
        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .claim("userId", userPrincipal.getId())
                .claim("email", userPrincipal.getEmail())
                .claim("fullName", userPrincipal.getFullName())
                .claim("roles", roleNames)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Generates a JWT token from username.
     * Useful for refresh token scenarios or administrative operations.
     * 
     * @param username the username to include in the token
     * @return JWT token string
     */
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Generates a refresh token with extended expiration time.
     * Refresh tokens are used to obtain new access tokens without re-authentication.
     * 
     * @param username the username to include in the refresh token
     * @return refresh token string
     */
    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtRefreshExpirationMs);
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .claim("type", "refresh") // Mark as refresh token
                .compact();
    }
    
    /**
     * Extracts username from JWT token.
     * 
     * @param token the JWT token
     * @return username from token subject
     */
    public String getUsernameFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getSubject();
    }
    
    /**
     * Extracts user ID from JWT token.
     * 
     * @param token the JWT token
     * @return user ID from token claims
     */
    public Long getUserIdFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.get("userId", Long.class);
    }
    
    /**
     * Extracts email from JWT token.
     * 
     * @param token the JWT token
     * @return email from token claims
     */
    public String getEmailFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.get("email", String.class);
    }
    
    /**
     * Extracts full name from JWT token.
     * 
     * @param token the JWT token
     * @return full name from token claims
     */
    public String getFullNameFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.get("fullName", String.class);
    }
    
    /**
     * Extracts roles from JWT token.
     * 
     * @param token the JWT token
     * @return list of role names from token claims
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.get("roles", List.class);
    }

    /**
     * Extracts expiration date from JWT token.
     * 
     * @param token the JWT token
     * @return expiration date
     */
    public Date getExpirationFromJwtToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        return claims.getExpiration();
    }
    
    /**
     * Checks if JWT token is expired.
     * 
     * @param token the JWT token
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationFromJwtToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            logger.error("Error checking token expiration: {}", e.getMessage());
            return true; // Treat invalid tokens as expired
        }
    }
    
    /**
     * Validates JWT token.
     * 
     * Checks for:
     * - Valid signature
     * - Token not expired
     * - Token not malformed
     * - Required claims present
     * 
     * @param authToken the JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(authToken);
            
            return true;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("JWT token validation error: {}", e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Validates refresh token.
     * Similar to JWT validation but also checks for refresh token type.
     * 
     * @param refreshToken the refresh token to validate
     * @return true if refresh token is valid, false otherwise
     */
    public boolean validateRefreshToken(String refreshToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();
            
            // Check if it's marked as a refresh token
            String tokenType = (String) claims.get("type");
            return "refresh".equals(tokenType);
            
        } catch (Exception e) {
            logger.error("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets the signing key for JWT operations.
     * 
     * Creates a secure HMAC-SHA256 key from the configured secret.
     * In production, ensure the secret is:
     * - At least 256 bits (32 characters) long
     * - Randomly generated
     * - Stored securely (environment variables, key management systems)
     * 
     * @return SecretKey for JWT signing and verification
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    /**
     * Gets remaining time until token expiration in milliseconds.
     * 
     * @param token the JWT token
     * @return remaining time in milliseconds, or 0 if expired/invalid
     */
    public long getRemainingTime(String token) {
        try {
            Date expiration = getExpirationFromJwtToken(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining);
        } catch (Exception e) {
            logger.error("Error getting remaining time for token: {}", e.getMessage());
            return 0;
        }
    }
    
    /**
     * Creates a new token with extended expiration time.
     * Useful for implementing "remember me" functionality.
     * 
     * @param username the username
     * @param extendedExpirationMs extended expiration time in milliseconds
     * @return JWT token with extended expiration
     */
    public String generateExtendedToken(String username, long extendedExpirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + extendedExpirationMs);
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .claim("extended", true)
                .compact();
    }
}