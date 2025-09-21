package com.hrms.controller;

import com.hrms.config.SwaggerResponses;
import com.hrms.dto.ApiResponse;
import com.hrms.dto.auth.*;
import com.hrms.entity.User;
import com.hrms.security.jwt.JwtAuthenticationTokenFilter;
import com.hrms.security.jwt.JwtUtils;
import com.hrms.security.service.UserDetailsImpl;
import com.hrms.security.service.UserDetailsServiceImpl;
import com.hrms.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * REST Controller for authentication and user management operations.
 * 
 * This controller provides endpoints for:
 * - User authentication (login/logout) with HTTP-only cookies
 * - User registration and account creation
 * - JWT token management (refresh, validation) via cookies
 * - Password management operations
 * - Account status management
 * 
 * Security Features:
 * - HTTP-only cookie-based JWT authentication
 * - Enhanced XSS protection (tokens not accessible via JavaScript)
 * - CSRF protection with SameSite cookies
 * - Comprehensive input validation
 * - Detailed error handling with security considerations
 * - Automatic cookie management and cleanup
 * - Secure token transmission
 * 
 * Cookie-based Authentication:
 * - Access tokens stored in 'hrms_access_token' HTTP-only cookie
 * - Refresh tokens stored in 'hrms_refresh_token' HTTP-only cookie
 * - Cookies automatically included in requests by browser
 * - No manual token handling required by clients
 * 
 * @author HR Management System Team
 * @version 3.0 - Cookie-based Authentication
 * @since 2024-09-18
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "HTTP-only cookie-based authentication and user management APIs. All authentication is handled via secure cookies automatically managed by the browser.")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    
    /**
     * Authenticates user and returns JWT tokens via HTTP-only cookies.
     * 
     * This endpoint:
     * 1. Validates user credentials
     * 2. Generates JWT access and refresh tokens
     * 3. Sets tokens as secure HTTP-only cookies
     * 4. Updates user's last login timestamp
     * 5. Returns user profile information (no tokens in response body)
     * 
     * Cookie Security Features:
     * - HttpOnly: Prevents XSS attacks
     * - Secure: Only sent over HTTPS in production
     * - SameSite=Strict: Prevents CSRF attacks
     * - Path=/api: Limited scope
     * - Automatic expiration handling
     * 
     * @param loginRequest the login credentials
     * @param request HTTP request for cookie creation
     * @param response HTTP response for setting cookies
     * @return user profile information (tokens stored in cookies)
     */
    @PostMapping("/login")
    @Operation(summary = "User authentication with HTTP-only cookies", 
               description = "Authenticate user with username/email and password. " +
                           "JWT tokens are automatically stored in secure HTTP-only cookies. " +
                           "No manual token handling required - cookies are managed by the browser.")
    @SwaggerResponses.AuthenticationResponses
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, 
                                   HttpServletRequest request, 
                                   HttpServletResponse response) {
        logger.info("Login attempt for user: {}", loginRequest.getUsernameOrEmail());
        
        try {
            // Determine if login is with email or username
            String identifier = loginRequest.getNormalizedUsernameOrEmail();
            
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, loginRequest.getPassword())
            );
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Get authenticated user details
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            
            // Generate JWT tokens
            String accessToken = jwtUtils.generateJwtToken(authentication);
            String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());

            // Set HTTP-only cookies
            Cookie accessTokenCookie = JwtAuthenticationTokenFilter.createJwtCookie(
                JwtAuthenticationTokenFilter.JWT_COOKIE_NAME, 
                accessToken, 
                86400, // 24 hours in seconds
                request
            );
            
            Cookie refreshTokenCookie = JwtAuthenticationTokenFilter.createJwtCookie(
                JwtAuthenticationTokenFilter.REFRESH_COOKIE_NAME, 
                refreshToken, 
                604800, // 7 days in seconds
                request
            );

            // Add cookies to response
            response.addCookie(accessTokenCookie);
            response.addCookie(refreshTokenCookie);
            
            // Extract user roles
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toList());
            
            // Create JWT response without tokens (they're in cookies)
            JwtResponse jwtResponse = new JwtResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFullName(),
                roles
            );
            
            // Set token expiration times
            jwtResponse.setExpiresIn(System.currentTimeMillis() + 86400000); // 24 hours
            jwtResponse.setRefreshExpiresIn(System.currentTimeMillis() + 604800000); // 7 days
            jwtResponse.setEnabled(userDetails.isEnabled());
            jwtResponse.setAccountNonLocked(userDetails.isAccountNonLocked());
            
            // Update user's last login
            authService.updateLastLogin(userDetails.getUsername());
            
            logger.info("User authenticated successfully with HTTP-only cookies: {} with roles: {}", 
                       userDetails.getUsername(), roles);
            
            return ResponseEntity.ok(ApiResponse.success("Login successful - authentication cookies set", jwtResponse));
            
        } catch (BadCredentialsException e) {
            logger.warn("Failed login attempt - invalid credentials for: {}", 
                       loginRequest.getUsernameOrEmail());
            
            // Record failed attempt for account lockout
            authService.recordFailedLoginAttempt(loginRequest.getNormalizedUsernameOrEmail());
            
            return new ResponseEntity<>(ApiResponse.error("Invalid username/email or password"), HttpStatus.UNAUTHORIZED);
                    
        } catch (LockedException e) {
            logger.warn("Login attempt for locked account: {}", loginRequest.getUsernameOrEmail());
            
            return new ResponseEntity<>(ApiResponse.error("Account is locked due to multiple failed login attempts"), HttpStatus.LOCKED);
                                              
        } catch (DisabledException e) {
            logger.warn("Login attempt for disabled account: {}", loginRequest.getUsernameOrEmail());
            
            return new ResponseEntity<>(ApiResponse.error("Account is disabled"), HttpStatus.FORBIDDEN);
                    
        } catch (AuthenticationException e) {
            logger.error("Authentication error for user: {} - {}", 
                        loginRequest.getUsernameOrEmail(), e.getMessage());
            
            return new ResponseEntity<>(ApiResponse.error("Authentication failed"), HttpStatus.UNAUTHORIZED);
                    
        } catch (Exception e) {
            logger.error("Unexpected error during login for user: {} - {}", 
                        loginRequest.getUsernameOrEmail(), e.getMessage(), e);
            
            return new ResponseEntity<>(ApiResponse.error("Authentication service unavailable"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Registers a new user account.
     * 
     * This endpoint:
     * 1. Validates registration data
     * 2. Checks username and email uniqueness
     * 3. Creates new user account with encrypted password
     * 4. Assigns appropriate roles
     * 5. Returns success message
     * 
     * @param signupRequest the registration data
     * @return success or error message
     */
    @PostMapping("/register")
    @Operation(summary = "User registration", 
               description = "Register a new user account with validation and role assignment")
    @SwaggerResponses.AuthenticationResponses
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody SignupRequest signupRequest) {
        logger.info("Registration attempt for username: {}, email: {}", 
                   signupRequest.getUsername(), signupRequest.getEmail());
        
        try {
            // Additional validation
            if (!signupRequest.isPasswordConfirmed()) {
                return new ResponseEntity<>(ApiResponse.error("Password and confirm password do not match"), HttpStatus.BAD_REQUEST);
            }
            
            // Create user account
            User user = authService.createUser(signupRequest);
            
            logger.info("User registered successfully - ID: {}, Username: {}", 
                       user.getId(), user.getUsername());
            
            return new ResponseEntity<>(ApiResponse.success(
                "User registered successfully! You can now login with your credentials.", null), HttpStatus.CREATED);
                    
        } catch (Exception e) {
            logger.error("Registration failed for username: {} - {}", 
                        signupRequest.getUsername(), e.getMessage());
            
            // Return appropriate error based on exception type
            if (e.getMessage().contains("already taken") || e.getMessage().contains("already in use")) {
                return new ResponseEntity<>(ApiResponse.error(e.getMessage()), HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>(ApiResponse.error("Registration failed: " + e.getMessage()), HttpStatus.BAD_REQUEST);
            }
        }
    }
    
    /**
     * Token refresh endpoint using HTTP-only cookies.
     * 
     * This endpoint:
     * 1. Extracts refresh token from HTTP-only cookie
     * 2. Validates the refresh token
     * 3. Generates new access token
     * 4. Updates access token cookie with new token
     * 5. Returns success status (new token stored in cookie)
     * 
     * Cookie Management:
     * - Automatically reads refresh token from 'hrms_refresh_token' cookie
     * - Updates 'hrms_access_token' cookie with new access token
     * - No manual token handling required
     * 
     * @param request HTTP request containing refresh token cookie
     * @param response HTTP response for setting new access token cookie
     * @return refresh response with user information
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token via cookies", 
               description = "Refresh access token using HTTP-only refresh token cookie. " +
                           "New access token is automatically stored in HTTP-only cookie. " +
                           "No request body required - refresh token read from cookies automatically.")
    @SwaggerResponses.AuthenticationResponses
    public ResponseEntity<ApiResponse<Object>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        
        try {
            // Extract refresh token from cookie
            String refreshToken = extractRefreshTokenFromCookie(request);
            
            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Refresh token not found in cookies", null));
            }
            
            if (!jwtUtils.validateRefreshToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid or expired refresh token", null));
            }
            
            String username = jwtUtils.getUsernameFromJwtToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // Generate new access token
            Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
            String newAccessToken = jwtUtils.generateJwtToken(auth);
            
            // Set new access token cookie
            Cookie accessTokenCookie = JwtAuthenticationTokenFilter.createJwtCookie(
                JwtAuthenticationTokenFilter.JWT_COOKIE_NAME, 
                newAccessToken, 
                86400, // 24 hours
                request
            );
            
            response.addCookie(accessTokenCookie);
            
            logger.info("Access token refreshed successfully for user: {}", username);
            
            return ResponseEntity.ok(new ApiResponse<>(
                true, 
                "Access token refreshed successfully", 
                Map.of("expiresIn", System.currentTimeMillis() + 86400000)
            ));
            
        } catch (Exception e) {
            logger.error("Token refresh failed: {}", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, "Token refresh failed", null));
        }
    }
    
    /**
     * User logout endpoint - clears HTTP-only authentication cookies.
     * 
     * This endpoint:
     * 1. Clears the security context
     * 2. Creates "clear" cookies with expired dates
     * 3. Clears both access and refresh token cookies
     * 4. Returns logout success confirmation
     * 
     * Cookie Management:
     * - Automatically clears 'hrms_access_token' cookie
     * - Automatically clears 'hrms_refresh_token' cookie
     * - Sets cookies to expire immediately for secure logout
     * - No manual token handling required
     * 
     * @param request HTTP request for cookie path information
     * @param response HTTP response for clearing authentication cookies
     * @return logout response confirmation
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout via cookies", 
               description = "Secure logout by clearing HTTP-only authentication cookies. " +
                           "Automatically clears both access and refresh token cookies. " +
                           "No request body required - cookies cleared automatically.")
    @SwaggerResponses.LogoutResponses
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        
        // Clear authentication context
        SecurityContextHolder.clearContext();
        
        // Clear cookies
        Cookie clearAccessCookie = JwtAuthenticationTokenFilter.createClearCookie(
            JwtAuthenticationTokenFilter.JWT_COOKIE_NAME, request);
        Cookie clearRefreshCookie = JwtAuthenticationTokenFilter.createClearCookie(
            JwtAuthenticationTokenFilter.REFRESH_COOKIE_NAME, request);
        
        response.addCookie(clearAccessCookie);
        response.addCookie(clearRefreshCookie);
        
        logger.info("User logged out successfully - cookies cleared");
        
        return ResponseEntity.ok(ApiResponse.success("Logout successful - authentication cookies cleared", null));
    }
    
    /**
     * Validates JWT token.
     * 
     * This endpoint can be used by clients to check if their token is still valid
     * without making a full API call.
     * 
     * @param token the JWT token to validate
     * @return token validation result
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token", 
               description = "Check if JWT token is valid and not expired")
    @SwaggerResponses.AuthenticationResponses
    public ResponseEntity<ApiResponse<Object>> validateToken(@Parameter(description = "JWT token to validate") 
                                         @RequestParam String token) {
        try {
            if (jwtUtils.validateJwtToken(token)) {
                String username = jwtUtils.getUsernameFromJwtToken(token);
                long remainingTime = jwtUtils.getRemainingTime(token);
                
                return ResponseEntity.ok(ApiResponse.success("Token is valid", 
                    new TokenValidationResponse(username, remainingTime)));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token is invalid or expired"));
            }
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Token validation failed"));
        }
    }
    
    /**
     * Checks username availability for registration.
     * 
     * @param username the username to check
     * @return availability status
     */
    @GetMapping("/check-username")
    @Operation(summary = "Check username availability", 
               description = "Check if username is available for registration")
    @SwaggerResponses.AuthenticationResponses
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkUsername(@Parameter(description = "Username to check") 
                                         @RequestParam String username) {
        boolean available = authService.isUsernameAvailable(username);
        
        return ResponseEntity.ok(ApiResponse.success("Username availability checked", 
                new AvailabilityResponse(username, available)));
    }
    
    /**
     * Checks email availability for registration.
     * 
     * @param email the email to check
     * @return availability status
     */
    @GetMapping("/check-email")
    @Operation(summary = "Check email availability", 
               description = "Check if email is available for registration")
    @SwaggerResponses.AuthenticationResponses
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkEmail(@Parameter(description = "Email to check") 
                                      @RequestParam String email) {
        boolean available = authService.isEmailAvailable(email);
        
        return ResponseEntity.ok(ApiResponse.success("Email availability checked", 
                new AvailabilityResponse(email, available)));
    }
    
    // Helper classes for API responses
    
    /**
     * Response DTO for token validation endpoint.
     */
    public static class TokenValidationResponse {
        private String username;
        private long remainingTimeMs;
        
        public TokenValidationResponse(String username, long remainingTimeMs) {
            this.username = username;
            this.remainingTimeMs = remainingTimeMs;
        }
        
        // Getters
        public String getUsername() { return username; }
        public long getRemainingTimeMs() { return remainingTimeMs; }
    }
    
    /**
     * Response DTO for availability check endpoints.
     */
    public static class AvailabilityResponse {
        private String value;
        private boolean available;
        
        public AvailabilityResponse(String value, boolean available) {
            this.value = value;
            this.available = available;
        }
        
        // Getters
        public String getValue() { return value; }
        public boolean isAvailable() { return available; }
    }
    
    /**
     * Extracts refresh token from HTTP-only cookie.
     * 
     * @param request HTTP request
     * @return refresh token or null if not found
     */
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JwtAuthenticationTokenFilter.REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
}