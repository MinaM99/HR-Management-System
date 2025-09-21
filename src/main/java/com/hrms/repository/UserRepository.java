package com.hrms.repository;

import com.hrms.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * 
 * Provides comprehensive data access methods for user management including:
 * - Authentication queries (username, email lookup)
 * - User status management (enabled/disabled, locked accounts)
 * - Security-related queries (failed login attempts)
 * - Advanced search and filtering capabilities
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username for authentication.
     * 
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email address.
     * 
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if username already exists.
     * 
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    Boolean existsByUsername(String username);
    
    /**
     * Check if email already exists.
     * 
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    Boolean existsByEmail(String email);
    
    /**
     * Find all enabled users.
     * 
     * @return list of enabled users
     */
    List<User> findByEnabledTrue();
    
    /**
     * Find all disabled users.
     * 
     * @return list of disabled users
     */
    List<User> findByEnabledFalse();
    
    /**
     * Find all locked accounts.
     * 
     * @return list of locked users
     */
    List<User> findByAccountNonLockedFalse();
    
    /**
     * Find users by role name with pagination.
     * 
     * @param roleName the role name to filter by
     * @param pageable pagination information
     * @return page of users with the specified role
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);
    
    /**
     * Find users created within a date range.
     * 
     * @param startDate the start date
     * @param endDate   the end date
     * @return list of users created within the date range
     */
    List<User> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find users who haven't logged in since a specific date.
     * 
     * @param date the date to compare against
     * @return list of inactive users
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL OR u.lastLogin < :date")
    List<User> findInactiveUsersSince(@Param("date") LocalDateTime date);
    
    /**
     * Find users with multiple failed login attempts.
     * 
     * @param threshold the minimum number of failed attempts
     * @return list of users with high failed login attempts
     */
    List<User> findByFailedLoginAttemptsGreaterThanEqual(Integer threshold);
    
    /**
     * Search users by username or full name containing the search term.
     * 
     * @param searchTerm the term to search for
     * @param pageable   pagination information
     * @return page of matching users
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchByUsernameOrFullName(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Update user's last login timestamp.
     * 
     * @param userId    the user ID
     * @param loginTime the login timestamp
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    /**
     * Reset failed login attempts for a user.
     * 
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") Long userId);
    
    /**
     * Increment failed login attempts for a user.
     * 
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") Long userId);
    
    /**
     * Lock user account by setting accountNonLocked to false.
     * 
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = false WHERE u.id = :userId")
    void lockAccount(@Param("userId") Long userId);
    
    /**
     * Unlock user account by setting accountNonLocked to true.
     * 
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = true WHERE u.id = :userId")
    void unlockAccount(@Param("userId") Long userId);
    
    /**
     * Enable user account by setting enabled to true.
     * 
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = true WHERE u.id = :userId")
    void enableAccount(@Param("userId") Long userId);
    
    /**
     * Disable user account by setting enabled to false.
     * 
     * @param userId the user ID
     */
    @Modifying
    @Query("UPDATE User u SET u.enabled = false WHERE u.id = :userId")
    void disableAccount(@Param("userId") Long userId);
    
    /**
     * Get user statistics for dashboard.
     * Returns array of [totalUsers, enabledUsers, disabledUsers, lockedUsers]
     * 
     * @return array containing user statistics
     */
    @Query("SELECT " +
           "COUNT(*) as totalUsers, " +
           "SUM(CASE WHEN u.enabled = true THEN 1 ELSE 0 END) as enabledUsers, " +
           "SUM(CASE WHEN u.enabled = false THEN 1 ELSE 0 END) as disabledUsers, " +
           "SUM(CASE WHEN u.accountNonLocked = false THEN 1 ELSE 0 END) as lockedUsers " +
           "FROM User u")
    Object[] getUserStatistics();
}