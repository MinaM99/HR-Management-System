package com.hrms.security.util;

import com.hrms.security.service.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

/**
 * Security utility class for JWT-based authentication context.
 * 
 * This utility provides convenient methods to access current user information
 * from the security context without needing database queries.
 * All user information is available directly from JWT tokens.
 * 
 * @author HR Management System Team
 * @version 2.0 - JWT-based authentication
 * @since 2024-09-23
 */
public class SecurityUtils {

    /**
     * Gets the current authenticated user's principal.
     * 
     * @return Optional containing UserPrincipal if authenticated, empty otherwise
     */
    public static Optional<UserPrincipal> getCurrentUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return Optional.of((UserPrincipal) authentication.getPrincipal());
        }
        
        return Optional.empty();
    }

    /**
     * Gets the current authenticated user's ID.
     * 
     * @return Optional containing user ID if authenticated, empty otherwise
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUserPrincipal().map(UserPrincipal::getId);
    }

    /**
     * Gets the current authenticated user's username.
     * 
     * @return Optional containing username if authenticated, empty otherwise
     */
    public static Optional<String> getCurrentUsername() {
        return getCurrentUserPrincipal().map(UserPrincipal::getUsername);
    }

    /**
     * Gets the current authenticated user's email.
     * 
     * @return Optional containing email if authenticated, empty otherwise
     */
    public static Optional<String> getCurrentUserEmail() {
        return getCurrentUserPrincipal().map(UserPrincipal::getEmail);
    }

    /**
     * Gets the current authenticated user's full name.
     * 
     * @return Optional containing full name if authenticated, empty otherwise
     */
    public static Optional<String> getCurrentUserFullName() {
        return getCurrentUserPrincipal().map(UserPrincipal::getFullName);
    }

    /**
     * Gets the current authenticated user's roles.
     * 
     * @return List of role names (without ROLE_ prefix) if authenticated, empty list otherwise
     */
    public static List<String> getCurrentUserRoles() {
        return getCurrentUserPrincipal()
                .map(UserPrincipal::getRoleNames)
                .orElse(List.of());
    }

    /**
     * Checks if the current user has a specific role.
     * 
     * @param roleName the role name to check (with or without ROLE_ prefix)
     * @return true if user has the role, false otherwise
     */
    public static boolean currentUserHasRole(String roleName) {
        return getCurrentUserPrincipal()
                .map(principal -> principal.hasRole(roleName))
                .orElse(false);
    }

    /**
     * Checks if a user is currently authenticated.
     * 
     * @return true if user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               authentication.getPrincipal() instanceof UserPrincipal;
    }

    /**
     * Gets the current authentication object.
     * 
     * @return Optional containing Authentication if present, empty otherwise
     */
    public static Optional<Authentication> getCurrentAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? Optional.of(authentication) : Optional.empty();
    }
}