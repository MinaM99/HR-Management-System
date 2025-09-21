package com.hrms.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

/**
 * Data Transfer Object for user registration/signup requests.
 * 
 * This DTO captures all the information needed to create a new user account
 * in the HR Management System. It includes comprehensive validation rules
 * to ensure data integrity and security.
 * 
 * Validation rules:
 * - Username: unique, alphanumeric with underscores/hyphens allowed
 * - Email: valid email format, will be unique in database
 * - Password: strong password requirements
 * - Full name: proper name format
 * - Roles: valid role assignments
 * 
 * Security considerations:
 * - Password should be encrypted before storage
 * - Username and email uniqueness checked at service layer
 * - Role assignments validated against existing roles
 * - Consider email verification for new accounts
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
public class SignupRequest {
    
    /**
     * Unique username for the new account.
     * Should be alphanumeric with optional underscores and hyphens.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", 
             message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;
    
    /**
     * Email address for the new account.
     * Will be used for notifications and password recovery.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;
    
    /**
     * Password for the new account.
     * Should meet strong password requirements.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 120, message = "Password must be between 8 and 120 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
             message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;
    
    /**
     * Password confirmation to ensure user typed it correctly.
     */
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
    
    /**
     * Full name of the user.
     */
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s'-]+$", 
             message = "Full name can only contain letters, spaces, apostrophes, and hyphens")
    private String fullName;
    
    /**
     * Roles to assign to the new user.
     * If not specified, will default to EMPLOYEE role.
     */
    private Set<String> roles;
    
    /**
     * Optional department ID if creating an employee account.
     */
    private Long departmentId;
    
    /**
     * Optional position title if creating an employee account.
     */
    private String position;
    
    /**
     * Flag to indicate if account should be enabled immediately.
     * Admin might want to create disabled accounts for approval workflow.
     */
    private Boolean enabled = true;
    
    // Constructors
    
    /**
     * Default constructor for JSON deserialization.
     */
    public SignupRequest() {
    }
    
    /**
     * Constructor with essential fields.
     *
     * @param username    unique username
     * @param email       email address
     * @param password    user password
     * @param fullName    user's full name
     */
    public SignupRequest(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmPassword = password; // Assume password is confirmed
        this.fullName = fullName;
    }
    
    // Getters and Setters
    
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
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    // Validation methods
    
    /**
     * Checks if password and confirmation password match.
     *
     * @return true if passwords match, false otherwise
     */
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
    
    /**
     * Gets normalized username (trimmed and lowercase).
     *
     * @return normalized username
     */
    public String getNormalizedUsername() {
        return username != null ? username.trim().toLowerCase() : null;
    }
    
    /**
     * Gets normalized email (trimmed and lowercase).
     *
     * @return normalized email
     */
    public String getNormalizedEmail() {
        return email != null ? email.trim().toLowerCase() : null;
    }
    
    /**
     * Checks if the signup request is for creating an employee account.
     * Based on presence of department ID or position.
     *
     * @return true if employee data is provided
     */
    public boolean isEmployeeSignup() {
        return departmentId != null || (position != null && !position.trim().isEmpty());
    }
    
    @Override
    public String toString() {
        return "SignupRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", roles=" + roles +
                ", departmentId=" + departmentId +
                ", position='" + position + '\'' +
                ", enabled=" + enabled +
                '}'; // Note: Passwords excluded from toString for security
    }
}