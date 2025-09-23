package com.hrms.security.jwt;

import com.hrms.security.service.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter for processing JWT tokens from HTTP-only cookies.
 * 
 * This filter:
 * 1. Extracts JWT token from HTTP-only cookies on each request
 * 2. Validates the token signature and expiration
 * 3. Loads user details and sets authentication context
 * 4. Handles authentication errors gracefully
 * 
 * Security Benefits of Cookie-based approach:
 * - HTTP-only cookies prevent XSS attacks
 * - Secure flag ensures HTTPS-only transmission
 * - SameSite attribute provides CSRF protection
 * 
 * @author HR Management System Team
 * @version 2.0 - Updated for HTTP-only cookie authentication
 */
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationTokenFilter.class);

    /**
     * Cookie name for storing JWT access token.
     */
    public static final String JWT_COOKIE_NAME = "hrms_access_token";

    /**
     * Cookie name for storing JWT refresh token.
     */
    public static final String REFRESH_COOKIE_NAME = "hrms_refresh_token";
    
    /**
     * Main filter method that processes JWT authentication for each request.
     * 
     * This method:
     * 1. Extracts JWT token from HTTP-only cookies
     * 2. Validates the token using JwtUtils
     * 3. Loads user details if token is valid
     * 4. Creates and sets authentication in Security Context
     * 5. Continues the filter chain
     * 
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if servlet processing fails
     * @throws IOException      if I/O processing fails
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Extract JWT token from HTTP-only cookie
            String jwt = parseJwtFromCookie(request);
            
            if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
                // Extract user information directly from JWT claims
                Long userId = jwtUtils.getUserIdFromJwtToken(jwt);
                String username = jwtUtils.getUsernameFromJwtToken(jwt);
                String email = jwtUtils.getEmailFromJwtToken(jwt);
                String fullName = jwtUtils.getFullNameFromJwtToken(jwt);
                List<String> roles = jwtUtils.getRolesFromJwtToken(jwt);

                // Create UserPrincipal directly from JWT claims (no database query needed)
                UserPrincipal userDetails = UserPrincipal.create(userId, username, email, fullName, roles);
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                logger.debug("Authentication set in security context for user: {}", username);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication from cookie: {}", e.getMessage());
            // Clear any existing authentication
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
    
    /**
     * Extracts JWT token from HTTP-only cookie.
     * 
     * @param request HTTP request containing cookies
     * @return JWT token if found, null otherwise
     */
    private String parseJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    String jwt = cookie.getValue();
                    if (StringUtils.hasText(jwt)) {
                        logger.debug("JWT token found in cookie: {}", JWT_COOKIE_NAME);
                        return jwt;
                    }
                }
            }
        }
        
        logger.debug("No JWT token found in cookies");
        return null;
    }

    /**
     * Creates an HTTP-only cookie with JWT token.
     * 
     * @param name cookie name
     * @param value JWT token value
     * @param maxAge cookie expiration in seconds
     * @param request HTTP request for domain/path context
     * @return configured Cookie instance
     */
    public static Cookie createJwtCookie(String name, String value, int maxAge, HttpServletRequest request) {
        Cookie cookie = new Cookie(name, value);
        
        // Security settings
        cookie.setHttpOnly(true);           // Prevent XSS attacks
        cookie.setSecure(isSecureRequest(request)); // HTTPS only in production
        cookie.setPath("/");                // Available to entire application
        cookie.setMaxAge(maxAge);           // Expiration time
        
        // SameSite attribute for CSRF protection (handled by application server)
        // cookie.setAttribute("SameSite", "Strict"); // Uncomment if your server supports it
        
        return cookie;
    }

    /**
     * Creates a cookie for clearing/removing existing JWT cookie.
     * 
     * @param name cookie name to clear
     * @param request HTTP request for domain/path context
     * @return configured Cookie for removal
     */
    public static Cookie createClearCookie(String name, HttpServletRequest request) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(isSecureRequest(request));
        cookie.setPath("/");
        cookie.setMaxAge(0); // Immediate expiration
        return cookie;
    }

    /**
     * Determines if request is secure (HTTPS).
     * 
     * @param request HTTP request
     * @return true if HTTPS or behind proxy, false otherwise
     */
    private static boolean isSecureRequest(HttpServletRequest request) {
        return request.isSecure() || 
               "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto")) ||
               "on".equalsIgnoreCase(request.getHeader("X-Forwarded-Ssl"));
    }
}