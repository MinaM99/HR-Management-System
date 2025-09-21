package com.hrms.security.service;

import com.hrms.entity.User;
import com.hrms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * 
 * This service is responsible for loading user details during authentication.
 * It serves as the bridge between Spring Security and our User entity/repository,
 * providing the necessary user information for authentication and authorization.
 * 
 * Key responsibilities:
 * - Load user details by username for authentication
 * - Convert User entities to UserDetails objects
 * - Handle user not found scenarios
 * - Support transactional operations for data consistency
 * 
 * Security considerations:
 * - Logs authentication attempts for monitoring
 * - Handles user not found exceptions gracefully
 * - Loads user roles/authorities for authorization
 * - Checks account status (enabled, locked, expired)
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Loads user details by username for Spring Security authentication.
     * 
     * This method is called by Spring Security during the authentication process.
     * It performs the following steps:
     * 
     * 1. Search for user by username in the database
     * 2. Throw UsernameNotFoundException if user not found
     * 3. Convert User entity to UserDetailsImpl
     * 4. Return UserDetails object for authentication
     * 
     * The method is transactional to ensure data consistency and to handle
     * lazy-loaded relationships (like user roles) properly.
     * 
     * @param username the username identifying the user whose data is required
     * @return UserDetails object containing user information and authorities
     * @throws UsernameNotFoundException if user with given username is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Attempting to load user details for username: {}", username);
        
        try {
            // Find user by username or email (support both login methods)
            User user = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElseThrow(() -> {
                        logger.warn("User not found with username or email: {}", username);
                        return new UsernameNotFoundException("User not found with username or email: " + username);
                    });
            
            // Log successful user lookup (without sensitive information)
            logger.debug("User found: {} - Enabled: {} - Account Non Locked: {} - Roles: {}", 
                        user.getUsername(), 
                        user.isEnabled(), 
                        user.isAccountNonLocked(),
                        user.getRoles().size());
            
            // Check if user account is in a valid state
            if (!user.getEnabled()) {
                logger.warn("Attempt to authenticate disabled user: {}", user.getUsername());
                // Note: We still return the UserDetails, but Spring Security will check isEnabled()
            }
            
            if (!user.getAccountNonLocked()) {
                logger.warn("Attempt to authenticate locked user: {}", user.getUsername());
                // Note: We still return the UserDetails, but Spring Security will check isAccountNonLocked()
            }
            
            // Convert User entity to UserDetailsImpl
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            
            logger.debug("Successfully loaded user details for: {}", user.getUsername());
            return userDetails;
            
        } catch (UsernameNotFoundException e) {
            // Re-throw UsernameNotFoundException as-is
            throw e;
            
        } catch (Exception e) {
            // Log unexpected errors and convert to authentication exception
            logger.error("Unexpected error loading user details for username: {} - Error: {}", 
                        username, e.getMessage(), e);
            
            throw new UsernameNotFoundException("Error loading user: " + username, e);
        }
    }
    
    /**
     * Loads user details by email address.
     * 
     * This is a convenience method for loading users by email instead of username.
     * Useful when the system allows login with either username or email.
     * 
     * @param email the email identifying the user whose data is required
     * @return UserDetails object containing user information and authorities
     * @throws UsernameNotFoundException if user with given email is not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByEmail(String email) throws UsernameNotFoundException {
        logger.debug("Attempting to load user details for email: {}", email);
        
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.warn("User not found with email: {}", email);
                        return new UsernameNotFoundException("User not found with email: " + email);
                    });
            
            logger.debug("User found by email: {} - Username: {} - Enabled: {}", 
                        email, user.getUsername(), user.isEnabled());
            
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            
            logger.debug("Successfully loaded user details for email: {}", email);
            return userDetails;
            
        } catch (UsernameNotFoundException e) {
            throw e;
            
        } catch (Exception e) {
            logger.error("Unexpected error loading user details for email: {} - Error: {}", 
                        email, e.getMessage(), e);
            
            throw new UsernameNotFoundException("Error loading user by email: " + email, e);
        }
    }
    
    /**
     * Loads user details by user ID.
     * 
     * This method is useful for loading user details when you have the user ID
     * (e.g., from a JWT token claim or session).
     * 
     * @param userId the user ID
     * @return UserDetails object containing user information and authorities
     * @throws UsernameNotFoundException if user with given ID is not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        logger.debug("Attempting to load user details for user ID: {}", userId);
        
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("User not found with ID: {}", userId);
                        return new UsernameNotFoundException("User not found with ID: " + userId);
                    });
            
            logger.debug("User found by ID: {} - Username: {} - Enabled: {}", 
                        userId, user.getUsername(), user.isEnabled());
            
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            
            logger.debug("Successfully loaded user details for user ID: {}", userId);
            return userDetails;
            
        } catch (UsernameNotFoundException e) {
            throw e;
            
        } catch (Exception e) {
            logger.error("Unexpected error loading user details for user ID: {} - Error: {}", 
                        userId, e.getMessage(), e);
            
            throw new UsernameNotFoundException("Error loading user by ID: " + userId, e);
        }
    }
    
    /**
     * Checks if a user exists by username.
     * 
     * @param username the username to check
     * @return true if user exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        try {
            return userRepository.existsByUsername(username);
        } catch (Exception e) {
            logger.error("Error checking if user exists by username: {} - Error: {}", 
                        username, e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if a user exists by email.
     * 
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        try {
            return userRepository.existsByEmail(email);
        } catch (Exception e) {
            logger.error("Error checking if user exists by email: {} - Error: {}", 
                        email, e.getMessage());
            return false;
        }
    }
}