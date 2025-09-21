package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Role entity for role-based access control (RBAC).
 * 
 * This entity represents system roles that define user permissions and access levels.
 * Implements GrantedAuthority for Spring Security integration.
 * 
 * Common roles in HR Management System:
 * - ADMIN: Full system access, user management
 * - HR: HR operations, employee management, leave approvals
 * - MANAGER: Team management, leave approvals for subordinates
 * - EMPLOYEE: Basic access, self-service operations
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2024-09-18
 */
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_name", columnList = "name", unique = true)
})
public class Role implements GrantedAuthority {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Role name (e.g., ADMIN, HR, MANAGER, EMPLOYEE).
     * Convention: Use uppercase with underscores for consistency.
     */
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;
    
    /**
     * Human-readable description of the role's purpose and permissions.
     */
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Column(name = "description", length = 255)
    private String description;
    
    /**
     * Timestamp when the role was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the role was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Users assigned to this role.
     * JsonIgnore prevents circular reference in JSON serialization.
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> users = new HashSet<>();
    
    // Constructors
    
    /**
     * Default constructor for JPA.
     */
    public Role() {
    }
    
    /**
     * Constructor for creating a role with name only.
     *
     * @param name the role name
     */
    public Role(String name) {
        this.name = name;
    }
    
    /**
     * Constructor for creating a role with name and description.
     *
     * @param name        the role name
     * @param description the role description
     */
    public Role(String name, String description) {
        this.name = name;
        this.description = description;
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
    
    // GrantedAuthority implementation for Spring Security
    
    /**
     * Returns the authority name for Spring Security.
     * Convention: Prefix with "ROLE_" for Spring Security compatibility.
     *
     * @return the authority string
     */
    @Override
    public String getAuthority() {
        return "ROLE_" + name;
    }
    
    // Standard getters and setters
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
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
    
    public Set<User> getUsers() {
        return users;
    }
    
    public void setUsers(Set<User> users) {
        this.users = users;
    }
    
    // Utility methods
    
    /**
     * Adds a user to this role.
     *
     * @param user the user to add
     */
    public void addUser(User user) {
        this.users.add(user);
        user.getRoles().add(this);
    }
    
    /**
     * Removes a user from this role.
     *
     * @param user the user to remove
     */
    public void removeUser(User user) {
        this.users.remove(user);
        user.getRoles().remove(this);
    }
    
    // Predefined role constants for consistency
    
    /**
     * Administrator role with full system access.
     */
    public static final String ADMIN = "ADMIN";
    
    /**
     * HR role with human resources management permissions.
     */
    public static final String HR = "HR";
    
    /**
     * Manager role with team and departmental management permissions.
     */
    public static final String MANAGER = "MANAGER";
    
    /**
     * Employee role with basic self-service permissions.
     */
    public static final String EMPLOYEE = "EMPLOYEE";
    
    // equals and hashCode for proper Set operations
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return name != null && name.equals(role.name);
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}