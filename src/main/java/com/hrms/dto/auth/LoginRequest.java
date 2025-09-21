package com.hrms.dto.auth;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for user login requests.
 * 
 * This DTO encapsulates the login credentials submitted by users during authentication.
 * It supports login with either username or email address for better user experience.
 * 
 * Validation rules:
 * - Username/email is required and cannot be blank
 * - Password is required with minimum length requirements
 * - Email validation when using email for login
 * 
 * Security considerations:
 * - Password field should not be logged or cached
 * - Validation helps prevent malformed requests
 * - Consider rate limiting on login endpoints
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
public class LoginRequest {
    
    /**
     * Username or email for authentication.
     * The system should support login with either username or email.
     */
    @NotBlank(message = "Username or email is required")
    @Size(min = 3, max = 100, message = "Username or email must be between 3 and 100 characters")
    private String usernameOrEmail;
    
    /**
     * User password for authentication.
     * Must meet minimum security requirements.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
    
    /**
     * Optional "remember me" flag for extended session.
     * When true, the system can issue longer-lived tokens.
     */
    private Boolean rememberMe = false;
    
    // Constructors
    
    /**
     * Default constructor for JSON deserialization.
     */
    public LoginRequest() {
    }
    
    /**
     * Constructor with username/email and password.
     *
     * @param usernameOrEmail username or email for login
     * @param password        user password
     */
    public LoginRequest(String usernameOrEmail, String password) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
    }
    
    /**
     * Constructor with all fields.
     *
     * @param usernameOrEmail username or email for login
     * @param password        user password
     * @param rememberMe      remember me flag
     */
    public LoginRequest(String usernameOrEmail, String password, Boolean rememberMe) {
        this.usernameOrEmail = usernameOrEmail;
        this.password = password;
        this.rememberMe = rememberMe;
    }
    
    // Getters and Setters
    
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }
    
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public Boolean getRememberMe() {
        return rememberMe;
    }
    
    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
    
    // Utility methods
    
    /**
     * Checks if the usernameOrEmail field contains an email address.
     *
     * @return true if it looks like an email, false otherwise
     */
    public boolean isEmailLogin() {
        return usernameOrEmail != null && usernameOrEmail.contains("@");
    }
    
    /**
     * Gets the trimmed and lowercase username or email.
     * Useful for consistent lookups.
     *
     * @return normalized username or email
     */
    public String getNormalizedUsernameOrEmail() {
        return usernameOrEmail != null ? usernameOrEmail.trim().toLowerCase() : null;
    }
    
    @Override
    public String toString() {
        return "LoginRequest{" +
                "usernameOrEmail='" + usernameOrEmail + '\'' +
                ", rememberMe=" + rememberMe +
                '}'; // Note: Password is excluded from toString for security
    }
}