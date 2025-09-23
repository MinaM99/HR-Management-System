package com.hrms.config;

import com.hrms.security.jwt.JwtAuthenticationEntryPoint;
import com.hrms.security.jwt.JwtAuthenticationTokenFilter;
import com.hrms.security.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security Configuration for HR Management System.
 * 
 * This configuration class sets up comprehensive security for the application including:
 * - JWT-based authentication and authorization
 * - CORS configuration for frontend integration
 * - CSRF protection management
 * - Method-level security for fine-grained access control
 * - Password encryption using BCrypt
 * - Custom authentication providers and filters
 * 
 * Security Architecture:
 * 1. JWT Authentication Filter processes tokens on each request
 * 2. Custom UserDetailsService loads user information
 * 3. DaoAuthenticationProvider handles authentication
 * 4. Method-level security provides endpoint protection
 * 5. Role-based access control using @PreAuthorize annotations
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig {
    
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    
    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;
    
    /**
     * Creates JWT authentication filter bean.
     * This filter processes JWT tokens on each request.
     * 
     * @return JwtAuthenticationTokenFilter instance
     */
    @Bean
    public JwtAuthenticationTokenFilter authenticationJwtTokenFilter() {
        return new JwtAuthenticationTokenFilter();
    }
    
    /**
     * Password encoder bean using BCrypt hashing algorithm.
     * 
     * BCrypt is recommended for password hashing because:
     * - It includes salt generation automatically
     * - It's computationally expensive (configurable rounds)
     * - It's resistant to rainbow table attacks
     * - It's adaptive (can increase rounds as hardware improves)
     * 
     * @return BCryptPasswordEncoder with default strength (10 rounds)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Custom DAO authentication provider.
     * 
     * This provider:
     * - Uses our custom UserDetailsService to load user information
     * - Uses BCrypt password encoder for password verification
     * - Supports account status checks (enabled, locked, expired)
     * 
     * @return configured DaoAuthenticationProvider
     */
    @Bean
    @SuppressWarnings("deprecation")
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        
        // Optional: Hide user not found exceptions for security
        // authProvider.setHideUserNotFoundExceptions(false);
        
        return authProvider;
    }
    
    /**
     * Authentication manager bean for programmatic authentication.
     * Used by authentication controller for login operations.
     * 
     * @param authConfig authentication configuration
     * @return AuthenticationManager instance
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * CORS configuration for cross-origin requests with HTTP-only cookie support.
     * 
     * This configuration allows:
     * - Specific frontend origins (configure for production)
     * - Common HTTP methods (GET, POST, PUT, DELETE, OPTIONS)
     * - Standard headers (removed Authorization as we use cookies now)
     * - Credentials for cookie-based authentication
     * 
     * Security Note: In production, replace "*" with specific frontend URLs
     * 
     * @return CorsConfigurationSource with configured origins and methods
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins in production
        // For development, allowing all origins. In production, specify exact URLs:
        // configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "https://yourdomain.com"));
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH"
        ));
        
        // Allow standard headers (removed Authorization as we use cookies)
        configuration.setAllowedHeaders(Arrays.asList(
            "Content-Type", "Accept", "X-Requested-With", 
            "Cache-Control", "Origin", "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // IMPORTANT: Must allow credentials for cookies to work
        configuration.setAllowCredentials(true);
        
        // Cache preflight responses for 1 hour
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    /**
     * Main security filter chain configuration.
     * 
     * This method configures:
     * 1. CORS and CSRF settings
     * 2. Session management (stateless for JWT)
     * 3. Exception handling for authentication failures
     * 4. URL-based security rules
     * 5. Custom authentication provider and filters
     * 
     * @param http HttpSecurity to configure
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS with our custom configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Disable CSRF for REST APIs (JWT tokens provide protection)
            .csrf(AbstractHttpConfigurer::disable)
            
            // Custom exception handling for authentication failures
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(unauthorizedHandler)
            )
            
            // Stateless session management (no session cookies)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configure URL-based authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - no authentication required
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/favicon.ico", "/error").permitAll()
                
                // Admin-only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "HR")
                
                // HR and Manager endpoints
                .requestMatchers("/api/departments/**").hasAnyRole("ADMIN", "HR", "MANAGER")
                .requestMatchers("/api/employees/**").hasAnyRole("ADMIN", "HR", "MANAGER")
                .requestMatchers("/api/leave-requests/approve/**", "/api/leave-requests/reject/**")
                    .hasAnyRole("ADMIN", "HR", "MANAGER")
                
                // Payroll endpoints - restricted access
                .requestMatchers("/api/payrolls/**").hasAnyRole("ADMIN", "HR")
                
                // Employee self-service endpoints
                .requestMatchers("/api/profile/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                .requestMatchers("/api/my-leave-requests/**").hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            );
        
        // Set custom authentication provider
        http.authenticationProvider(authenticationProvider());
        
        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(authenticationJwtTokenFilter(), 
                           UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}