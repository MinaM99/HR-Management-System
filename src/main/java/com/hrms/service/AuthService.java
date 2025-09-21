package com.hrms.service;

import com.hrms.dto.auth.SignupRequest;
import com.hrms.entity.Role;
import com.hrms.entity.User;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.RoleRepository;
import com.hrms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Service class for handling authentication and user management operations.
 * 
 * This service provides business logic for:
 * - User registration and account creation
 * - Password management and validation
 * - User account status management (enable/disable, lock/unlock)
 * - Role assignment and management
 * - Authentication security features
 * 
 * Security features:
 * - Password encryption using BCrypt
 * - Account lockout after failed login attempts
 * - User account status validation
 * - Role-based access control support
 * - Comprehensive audit logging
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    // Maximum failed login attempts before account lockout
    private static final int MAX_FAILED_ATTEMPTS = 5;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Creates a new user account based on signup request.
     * 
     * This method:
     * 1. Validates username and email uniqueness
     * 2. Encrypts the password
     * 3. Assigns default or specified roles
     * 4. Creates and saves the user entity
     * 5. Logs the account creation event
     * 
     * @param signupRequest the signup request containing user details
     * @return the created User entity
     * @throws DuplicateResourceException if username or email already exists
     * @throws ResourceNotFoundException if specified roles don't exist
     */
    public User createUser(SignupRequest signupRequest) {
        logger.info("Creating new user account for username: {}", signupRequest.getUsername());
        
        // Validate username uniqueness
        if (userRepository.existsByUsername(signupRequest.getNormalizedUsername())) {
            logger.warn("Attempt to create user with existing username: {}", signupRequest.getUsername());
            throw new DuplicateResourceException("Username is already taken: " + signupRequest.getUsername());
        }
        
        // Validate email uniqueness
        if (userRepository.existsByEmail(signupRequest.getNormalizedEmail())) {
            logger.warn("Attempt to create user with existing email: {}", signupRequest.getEmail());
            throw new DuplicateResourceException("Email is already in use: " + signupRequest.getEmail());
        }
        
        // Validate password confirmation
        if (!signupRequest.isPasswordConfirmed()) {
            throw new IllegalArgumentException("Password and confirmation password do not match");
        }
        
        // Create new user entity
        User user = new User();
        user.setUsername(signupRequest.getNormalizedUsername());
        user.setEmail(signupRequest.getNormalizedEmail());
        user.setFullName(signupRequest.getFullName());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setEnabled(signupRequest.getEnabled() != null ? signupRequest.getEnabled() : true);
        
        // Assign roles
        Set<Role> roles = assignRoles(signupRequest.getRoles());
        user.setRoles(roles);
        
        // Save user
        user = userRepository.save(user);
        
        logger.info("Successfully created user account - ID: {}, Username: {}, Roles: {}", 
                   user.getId(), user.getUsername(), 
                   user.getRoles().stream().map(Role::getName).toList());
        
        return user;
    }
    
    /**
     * Updates user's last login timestamp and resets failed login attempts.
     * Called after successful authentication.
     * 
     * @param username the username of the authenticated user
     */
    public void updateLastLogin(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            
            user.updateLastLogin();
            user.resetFailedLoginAttempts();
            userRepository.save(user);
            
            logger.debug("Updated last login for user: {}", username);
            
        } catch (Exception e) {
            logger.error("Error updating last login for user: {} - {}", username, e.getMessage());
            // Don't throw exception as this is not critical for authentication
        }
    }
    
    /**
     * Records a failed login attempt and locks account if threshold is reached.
     * 
     * @param username the username that failed authentication
     */
    public void recordFailedLoginAttempt(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElse(null);
            
            if (user != null) {
                user.incrementFailedLoginAttempts();
                
                // Lock account if max attempts reached
                if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                    user.setAccountNonLocked(false);
                    logger.warn("Account locked due to {} failed login attempts: {}", 
                               MAX_FAILED_ATTEMPTS, username);
                }
                
                userRepository.save(user);
                
                logger.debug("Recorded failed login attempt for user: {} (Total: {})", 
                           username, user.getFailedLoginAttempts());
            }
            
        } catch (Exception e) {
            logger.error("Error recording failed login attempt for user: {} - {}", username, e.getMessage());
        }
    }
    
    /**
     * Unlocks a user account and resets failed login attempts.
     * Typically called by administrators.
     * 
     * @param userId the ID of the user to unlock
     * @throws ResourceNotFoundException if user doesn't exist
     */
    public void unlockUserAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setAccountNonLocked(true);
        user.resetFailedLoginAttempts();
        userRepository.save(user);
        
        logger.info("User account unlocked - ID: {}, Username: {}", userId, user.getUsername());
    }
    
    /**
     * Enables or disables a user account.
     * 
     * @param userId  the ID of the user
     * @param enabled true to enable, false to disable
     * @throws ResourceNotFoundException if user doesn't exist
     */
    public void setUserAccountStatus(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        user.setEnabled(enabled);
        userRepository.save(user);
        
        logger.info("User account {} - ID: {}, Username: {}", 
                   enabled ? "enabled" : "disabled", userId, user.getUsername());
    }
    
    /**
     * Changes user password.
     * 
     * @param userId      the ID of the user
     * @param newPassword the new password (plain text, will be encrypted)
     * @throws ResourceNotFoundException if user doesn't exist
     */
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Validate password strength (implement your password policy here)
        validatePasswordStrength(newPassword);
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCredentialsNonExpired(true); // Reset credential expiration
        userRepository.save(user);
        
        logger.info("Password changed for user - ID: {}, Username: {}", userId, user.getUsername());
    }
    
    /**
     * Assigns roles to a user based on role names.
     * If no roles specified, assigns default EMPLOYEE role.
     * 
     * @param roleNames set of role names to assign
     * @return set of Role entities
     * @throws ResourceNotFoundException if any specified role doesn't exist
     */
    private Set<Role> assignRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        
        if (roleNames == null || roleNames.isEmpty()) {
            // Assign default EMPLOYEE role
            Role employeeRole = roleRepository.findByName(Role.EMPLOYEE)
                    .orElseThrow(() -> new ResourceNotFoundException("Default role not found: " + Role.EMPLOYEE));
            roles.add(employeeRole);
            
        } else {
            // Assign specified roles
            for (String roleName : roleNames) {
                Role role = roleRepository.findByName(roleName.toUpperCase())
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));
                roles.add(role);
            }
        }
        
        return roles;
    }
    
    /**
     * Validates password strength according to security policy.
     * Override this method to implement custom password policies.
     * 
     * @param password the password to validate
     * @throws IllegalArgumentException if password doesn't meet requirements
     */
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        
        // Additional password validation can be added here
        // - Check for uppercase, lowercase, numbers, special characters
        // - Check against common password lists
        // - Check password history
    }
    
    /**
     * Checks if a username is available for registration.
     * 
     * @param username the username to check
     * @return true if available, false if taken
     */
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username.trim().toLowerCase());
    }
    
    /**
     * Checks if an email is available for registration.
     * 
     * @param email the email to check
     * @return true if available, false if taken
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email.trim().toLowerCase());
    }
}