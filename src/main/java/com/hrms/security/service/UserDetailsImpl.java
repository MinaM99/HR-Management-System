package com.hrms.security.service;

import com.hrms.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

/**
 * Custom implementation of Spring Security's UserDetails interface.
 * 
 * This class wraps our User entity to provide the necessary information
 * for Spring Security authentication and authorization. It implements
 * UserDetails to integrate seamlessly with Spring Security's authentication
 * mechanisms.
 * 
 * Key features:
 * - Wraps User entity for Spring Security integration
 * - Provides user authorities (roles) for authorization
 * - Supports account status checks (enabled, locked, expired)
 * - Implements equals and hashCode for proper security context handling
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
public class UserDetailsImpl implements UserDetails {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean enabled;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    
    /**
     * Constructor to create UserDetailsImpl from User entity.
     * 
     * @param id                      user ID
     * @param username               username for authentication
     * @param email                  user email
     * @param fullName              user's full name
     * @param password              encrypted password
     * @param authorities           user authorities (roles)
     * @param enabled               account enabled status
     * @param accountNonExpired     account expiration status
     * @param accountNonLocked      account locked status
     * @param credentialsNonExpired credentials expiration status
     */
    public UserDetailsImpl(Long id, String username, String email, String fullName, String password,
                          Collection<? extends GrantedAuthority> authorities,
                          boolean enabled, boolean accountNonExpired, 
                          boolean accountNonLocked, boolean credentialsNonExpired) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonExpired = accountNonExpired;
        this.accountNonLocked = accountNonLocked;
        this.credentialsNonExpired = credentialsNonExpired;
    }
    
    /**
     * Factory method to create UserDetailsImpl from User entity.
     * 
     * This method converts our User entity into a UserDetails implementation
     * that Spring Security can work with. It maps all the necessary fields
     * and converts roles to authorities.
     * 
     * @param user the User entity
     * @return UserDetailsImpl instance
     */
    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPassword(),
                user.getAuthorities(), // User.getRoles() returns Collection<? extends GrantedAuthority>
                user.isEnabled(),
                user.isAccountNonExpired(),
                user.isAccountNonLocked(),
                user.isCredentialsNonExpired()
        );
    }
    
    // UserDetails interface implementation
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
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
    
    // Additional getters for accessing user information
    
    /**
     * Gets the user ID.
     * 
     * @return user ID
     */
    public Long getId() {
        return id;
    }
    
    /**
     * Gets the user's email address.
     * 
     * @return email address
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * Gets the user's full name.
     * 
     * @return full name
     */
    public String getFullName() {
        return fullName;
    }
    
    // equals and hashCode implementation for proper handling in security contexts
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetailsImpl)) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id) && 
               Objects.equals(username, user.username);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
    
    @Override
    public String toString() {
        return "UserDetailsImpl{" +
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