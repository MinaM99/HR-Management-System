package com.hrms.security.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Simplified UserPrincipal for JWT-based authentication.
 * 
 * This class represents the authenticated user principal that is created from JWT token claims.
 * It eliminates the need for database queries during each request authentication.
 * 
 * Key features:
 * - Contains all necessary user information from JWT token
 * - Implements UserDetails for Spring Security integration
 * - No database dependencies for role/authority resolution
 * - Lightweight and stateless
 * 
 * @author HR Management System Team
 * @version 2.0 - JWT-based authentication
 * @since 2024-09-23
 */
public class UserPrincipal implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String email;
    private final String fullName;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;

    /**
     * Constructor to create UserPrincipal from JWT token claims.
     * 
     * @param id                      user ID
     * @param username               username for authentication
     * @param email                  user email
     * @param fullName              user's full name
     * @param roles                 user roles from JWT
     * @param enabled               account enabled status (default true for JWT auth)
     * @param accountNonExpired     account expiration status (default true for JWT auth)
     * @param accountNonLocked      account locked status (default true for JWT auth)
     * @param credentialsNonExpired credentials expiration status (default true for JWT auth)
     */
    public UserPrincipal(Long id, String username, String email, String fullName, 
                        List<String> roles, boolean enabled, boolean accountNonExpired,
                        boolean accountNonLocked, boolean credentialsNonExpired) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.authorities = roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
    }

    /**
     * Factory method to create UserPrincipal from JWT token claims.
     * Uses default account status values suitable for JWT authentication.
     * 
     * @param id       user ID from JWT
     * @param username username from JWT
     * @param email    email from JWT
     * @param fullName full name from JWT
     * @param roles    roles from JWT
     * @return UserPrincipal instance
     */
    public static UserPrincipal create(Long id, String username, String email, String fullName, List<String> roles) {
        return new UserPrincipal(
                id, username, email, fullName, roles,
                true, // enabled - if user has valid JWT, assume enabled
                true, // accountNonExpired - JWT expiration handles this
                true, // accountNonLocked - if user has valid JWT, assume not locked
                true  // credentialsNonExpired - JWT expiration handles this
        );
    }

    // Getters for user information

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    // UserDetails interface implementation

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // Password is not needed for JWT authentication
        return null;
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

    // Utility methods

    /**
     * Checks if the user has a specific role.
     *
     * @param roleName the role name to check (with or without ROLE_ prefix)
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String roleName) {
        String roleToCheck = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(roleToCheck));
    }

    /**
     * Gets list of role names without ROLE_ prefix.
     *
     * @return list of role names
     */
    public List<String> getRoleNames() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("ROLE_", ""))
                .collect(Collectors.toList());
    }

    // Object methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserPrincipal)) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", authorities=" + authorities.size() + " authorities" +
                ", enabled=" + enabled +
                ", accountNonExpired=" + accountNonExpired +
                ", accountNonLocked=" + accountNonLocked +
                ", credentialsNonExpired=" + credentialsNonExpired +
                '}';
    }
}