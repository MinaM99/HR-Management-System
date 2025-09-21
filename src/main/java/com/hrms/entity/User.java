package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity representing system users for authentication and authorization.
 * 
 * This entity implements UserDetails for Spring Security integration and follows
 * security best practices including:
 * - Password encryption (never store plain text passwords)
 * - Account status management (enabled/disabled accounts)
 * - Role-based access control
 * - Audit trail with timestamps
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_enabled", columnList = "enabled")
})
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique username for authentication.
     * Must be between 3-50 characters and cannot be blank.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * User's email address. Must be unique and valid.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    /**
     * Encrypted password. Never store plain text passwords!
     * This field is marked with @JsonIgnore to prevent password exposure in API responses.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 120, message = "Password must be between 8 and 120 characters")
    @Column(name = "password", nullable = false, length = 120)
    @JsonIgnore
    private String password;
    
    /**
     * User's full name for display purposes.
     */
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    /**
     * Account status. Disabled accounts cannot authenticate.
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    /**
     * Account locked status. Locked accounts cannot authenticate.
     * Used for security purposes (e.g., too many failed login attempts).
     */
    @Column(name = "account_non_locked", nullable = false)
    private Boolean accountNonLocked = true;
    
    /**
     * Credentials (password) expiration status.
     * Used for enforcing password policy.
     */
    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;
    
    /**
     * Account expiration status.
     * Used for temporary accounts or account lifecycle management.
     */
    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;
    
    /**
     * Timestamp when the account was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the account was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Timestamp of last successful login.
     * Used for security monitoring and user analytics.
     */
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    /**
     * Number of failed login attempts.
     * Used for account lockout security measures.
     */
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;
    
    /**
     * User roles for authorization.
     * Many-to-many relationship allows users to have multiple roles.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    
    /**
     * One-to-one relationship with Employee entity.
     * Not all users are employees (e.g., system administrators).
     */
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Employee employee;
    
    // Constructors
    
    /**
     * Default constructor for JPA.
     */
    public User() {
    }
    
    /**
     * Constructor for creating a new user with basic information.
     *
     * @param username the unique username
     * @param email    the user's email address
     * @param password the encrypted password
     * @param fullName the user's full name
     */
    public User(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }
    
    // Lifecycle callbacks for automatic timestamp management
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // UserDetails implementation methods for Spring Security integration
    
    /**
     * Returns the authorities granted to the user.
     * This method converts roles to Spring Security authorities.
     *
     * @return collection of granted authorities
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    // Standard getters and setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
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
    
    public Boolean getCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    public void setCredentialsNonExpired(Boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }
    
    public Boolean getAccountNonExpired() {
        return accountNonExpired;
    }
    
    public void setAccountNonExpired(Boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
    
    public void setFailedLoginAttempts(Integer failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }
    
    public Set<Role> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    public Employee getEmployee() {
        return employee;
    }
    
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    
    // Utility methods
    
    /**
     * Adds a role to this user.
     *
     * @param role the role to add
     */
    public void addRole(Role role) {
        this.roles.add(role);
    }
    
    /**
     * Removes a role from this user.
     *
     * @param role the role to remove
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
    }
    
    /**
     * Checks if user has a specific role.
     *
     * @param roleName the role name to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName().equals(roleName));
    }
    
    /**
     * Increments failed login attempts counter.
     */
    public void incrementFailedLoginAttempts() {
        this.failedLoginAttempts++;
    }
    
    /**
     * Resets failed login attempts counter.
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
    }
    
    /**
     * Updates last login timestamp to current time.
     */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", enabled=" + enabled +
                ", roles=" + roles.size() + " roles" +
                '}';
    }
}