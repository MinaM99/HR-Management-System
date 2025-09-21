package com.hrms.repository;

import com.hrms.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    /**
     * Find department by name
     */
    Optional<Department> findByName(String name);
    
    /**
     * Find departments by name containing (case-insensitive)
     */
    List<Department> findByNameContainingIgnoreCase(String name);
    
    /**
     * Check if department exists by name
     */
    boolean existsByName(String name);
    
    /**
     * Find all departments ordered by name
     */
    List<Department> findAllByOrderByNameAsc();
    
    /**
     * Get department with employee count
     */
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees WHERE d.id = :id")
    Optional<Department> findByIdWithEmployees(@Param("id") Long id);
    
    /**
     * Get departments with employee count
     */
    @Query("SELECT d, COUNT(e) FROM Department d LEFT JOIN d.employees e GROUP BY d")
    List<Object[]> findDepartmentsWithEmployeeCount();
}