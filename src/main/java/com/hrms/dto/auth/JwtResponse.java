package com.hrms.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * JWT Response DTO for cookie-based authentication responses.
 * 
 * Note: In cookie-based authentication, tokens are NOT included in response body.
 * They are set as HTTP-only cookies for security.
 * 
 * @version 2.0 - Updated for HTTP-only cookie authentication
 */
@Schema(description = "Authentication response containing user details")
public class JwtResponse {
    
    @Schema(description = "User ID", example = "1")
    private Long id;
    
    @Schema(description = "Username", example = "admin")
    private String username;
    
    @Schema(description = "User email", example = "admin@company.com")
    private String email;
    
    @Schema(description = "User full name", example = "System Administrator")
    private String fullName;
    
    @Schema(description = "User roles", example = "[\"ADMIN\", \"HR\"]")
    private List<String> roles;
    
    @Schema(description = "Access token expiration timestamp", example = "1726689049768")
    private Long expiresIn;
    
    @Schema(description = "Refresh token expiration timestamp", example = "1727293849768")
    private Long refreshExpiresIn;

    @Schema(description = "Whether user account is enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "Whether user account is not locked", example = "true") 
    private Boolean accountNonLocked;
    
    // Constructors
    
    /**
     * Default constructor for JSON serialization.
     */
    public JwtResponse() {
    }
    
    /**
     * Constructor with essential fields for cookie-based authentication.
     *
     * @param id           user ID
     * @param username     username
     * @param email        user email
     * @param fullName     user's full name
     * @param roles        list of user roles
     */
    public JwtResponse(Long id, String username, String email, String fullName, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
    }
    
    /**
     * Constructor with all fields for cookie-based authentication.
     *
     * @param id                user ID
     * @param username          username
     * @param email             user email
     * @param fullName          user's full name
     * @param roles             list of user roles
     * @param expiresIn         access token expiration
     * @param refreshExpiresIn  refresh token expiration
     * @param enabled           account enabled status
     * @param accountNonLocked  account locked status
     */
    public JwtResponse(Long id, String username, String email, String fullName, List<String> roles, 
                      Long expiresIn, Long refreshExpiresIn, Boolean enabled, Boolean accountNonLocked) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
        this.expiresIn = expiresIn;
        this.refreshExpiresIn = refreshExpiresIn;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
    }
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public Long getRefreshExpiresIn() {
        return refreshExpiresIn;
    }
    
    public void setRefreshExpiresIn(Long refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Boolean getAccountNonLocked() {
        return accountNonLocked;
    }
    
    public void setAccountNonLocked(Boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }
    
    // Utility methods
    
    /**
     * Checks if the user has a specific role.
     *
     * @param roleName the role name to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String roleName) {
        return roles != null && roles.contains(roleName);
    }
    
    /**
     * Gets a display-friendly version of roles.
     *
     * @return comma-separated roles string
     */
    public String getRolesAsString() {
        return roles != null ? String.join(", ", roles) : "";
    }
    
    /**
     * Checks if the access token is close to expiration (within 5 minutes).
     * Useful for proactive token refresh.
     *
     * @return true if token expires soon, false otherwise
     */
    public boolean isTokenExpiringSoon() {
        if (expiresIn == null) return false;
        long currentTime = System.currentTimeMillis();
        long fiveMinutes = 5 * 60 * 1000; // 5 minutes in milliseconds
        return (expiresIn - currentTime) <= fiveMinutes;
    }
    
    @Override
    public String toString() {
        return "JwtResponse{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", roles=" + roles +
                ", expiresIn=" + expiresIn +
                ", refreshExpiresIn=" + refreshExpiresIn +
                ", enabled=" + enabled +
                ", accountNonLocked=" + accountNonLocked +
                '}'; // Note: Tokens not included - they are in HTTP-only cookies
    }
}