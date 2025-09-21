package com.hrms.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Authentication Entry Point for handling unauthorized access attempts.
 * 
 * This component is invoked when a user tries to access a secured REST resource
 * without providing valid authentication credentials. It returns a standardized
 * error response in JSON format instead of redirecting to a login page.
 * 
 * Key responsibilities:
 * - Handle authentication failures for JWT-secured endpoints
 * - Return consistent error responses
 * - Log security events for monitoring
 * - Prevent information disclosure through error messages
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    
    /**
     * Handles authentication failures by returning a standardized error response.
     * 
     * This method is called when:
     * - No JWT token is provided in the request
     * - Invalid or expired JWT token is provided
     * - User tries to access protected resources without authentication
     * 
     * The response includes:
     * - HTTP 401 Unauthorized status
     * - JSON error message with details
     * - Timestamp and request path for debugging
     * 
     * @param request       the HTTP request that resulted in an authentication failure
     * @param response      the HTTP response
     * @param authException the exception that caused the authentication failure
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        // Log the authentication failure for security monitoring
        logger.error("Unauthorized error: {} - Path: {} - Method: {} - Remote Address: {}", 
                    authException.getMessage(), 
                    request.getRequestURI(),
                    request.getMethod(),
                    getClientIpAddress(request));
        
        // Set response content type to JSON
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        // Set HTTP status to 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // Create error response body
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "Authentication required to access this resource");
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("path", request.getRequestURI());
        
        // Add additional details based on the exception type
        if (authException.getMessage().contains("token")) {
            errorResponse.put("details", "Invalid or expired authentication token");
        } else {
            errorResponse.put("details", "Please provide valid authentication credentials");
        }
        
        // Convert error response to JSON and write to response body
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), errorResponse);
        
        // Ensure the response is committed
        response.getOutputStream().flush();
    }
    
    /**
     * Extracts the client IP address from the HTTP request.
     * 
     * This method checks various headers that might contain the real client IP
     * when the application is behind a proxy or load balancer:
     * - X-Forwarded-For: Standard header for forwarded requests
     * - X-Real-IP: Alternative header used by some proxies
     * - Remote address: Direct connection IP (fallback)
     * 
     * @param request the HTTP request
     * @return the client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            return xForwardedForHeader.split(",")[0].trim();
        }
        
        String xRealIpHeader = request.getHeader("X-Real-IP");
        if (xRealIpHeader != null && !xRealIpHeader.isEmpty()) {
            return xRealIpHeader;
        }
        
        // Fallback to remote address
        return request.getRemoteAddr();
    }
}