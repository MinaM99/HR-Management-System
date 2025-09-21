package com.hrms.repository;

import com.hrms.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Role entity operations.
 * 
 * Provides data access methods for role management including:
 * - Role lookup by name
 * - Role existence checks
 * - Statistics and reporting queries
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * Find role by name.
     * 
     * @param name the role name to search for
     * @return Optional containing the role if found
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if role name already exists.
     * 
     * @param name the role name to check
     * @return true if role exists, false otherwise
     */
    Boolean existsByName(String name);
    
    /**
     * Find roles by name containing the search term (case-insensitive).
     * 
     * @param searchTerm the term to search for in role names
     * @return list of matching roles
     */
    @Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Role> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    
    /**
     * Get role statistics including user count per role.
     * Returns array of [roleName, userCount]
     * 
     * @return list of role statistics
     */
    @Query("SELECT r.name, COUNT(u) FROM Role r LEFT JOIN r.users u GROUP BY r.id, r.name ORDER BY r.name")
    List<Object[]> getRoleStatistics();
    
    /**
     * Find roles that have no users assigned.
     * 
     * @return list of unused roles
     */
    @Query("SELECT r FROM Role r WHERE r.users IS EMPTY")
    List<Role> findUnusedRoles();
    
    /**
     * Find roles assigned to a specific user.
     * 
     * @param userId the user ID
     * @return list of roles for the user
     */
    @Query("SELECT r FROM Role r JOIN r.users u WHERE u.id = :userId")
    List<Role> findByUserId(@Param("userId") Long userId);
    
    /**
     * Count users per role.
     * 
     * @param roleName the role name
     * @return number of users with the specified role
     */
    @Query("SELECT COUNT(u) FROM Role r JOIN r.users u WHERE r.name = :roleName")
    Long countUsersByRoleName(@Param("roleName") String roleName);
}