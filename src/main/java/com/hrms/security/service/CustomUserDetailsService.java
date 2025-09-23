package com.hrms.security.service;

import com.hrms.entity.User;
import com.hrms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Professional UserDetailsService implementation for JWT-based authentication.
 * 
 * This service is used only during the initial authentication process (login).
 * After login, all user information and roles are stored in JWT tokens,
 * eliminating the need for database queries during request processing.
 * 
 * Key responsibilities:
 * - Load user details by username/email for initial authentication
 * - Validate user credentials during login
 * - Return User entity as UserDetails (User implements UserDetails)
 * 
 * Note: This service is NOT used for request-level authorization.
 * User roles and authorities are extracted directly from JWT tokens.
 * 
 * @author HR Management System Team
 * @version 3.0 - Professional implementation with direct User entity usage
 * @since 2024-09-23
 */
@Service
public class CustomUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Loads user details by username OR email for authentication.
     * 
     * This method is called only during the login process to validate
     * user credentials. It accepts both username and email addresses
     * as login identifiers for better user experience.
     * 
     * After successful authentication, user information is embedded 
     * in JWT tokens and no further database queries are needed.
     * 
     * Returns the User entity directly since it implements UserDetails.
     * 
     * @param usernameOrEmail the username OR email identifying the user
     * @return User entity (which implements UserDetails) containing user information and authorities
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        logger.debug("Loading user details for authentication by username or email: {}", usernameOrEmail);
        
        try {
            // Find user by username or email (support both login methods)
            User user = userRepository.findByUsername(usernameOrEmail)
                    .or(() -> userRepository.findByEmail(usernameOrEmail))
                    .orElseThrow(() -> {
                        logger.warn("User not found with username or email: {}", usernameOrEmail);
                        return new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
                    });
            
            // Check if user account is in a valid state
            if (!user.getEnabled()) {
                logger.warn("Attempt to authenticate disabled user: {}", usernameOrEmail);
                throw new UsernameNotFoundException("User account is disabled: " + usernameOrEmail);
            }
            
            // Log successful user lookup (without sensitive information)
            logger.debug("User found for authentication: {} - Enabled: {} - Account Non Locked: {} - Roles: {}", 
                        user.getUsername(), 
                        user.isEnabled(), 
                        user.isAccountNonLocked(),
                        user.getRoles().size());
            
            logger.debug("Successfully loaded user details for authentication by username or email: {}", usernameOrEmail);
            
            // Return User entity directly (it implements UserDetails)
            return user;
            
        } catch (UsernameNotFoundException e) {
            throw e;
            
        } catch (Exception e) {
            logger.error("Unexpected error loading user details for authentication: {} - Error: {}", 
                        usernameOrEmail, e.getMessage(), e);
            throw new UsernameNotFoundException("Error loading user details: " + usernameOrEmail, e);
        }
    }
}