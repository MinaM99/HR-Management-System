package com.hrms.repository;

import com.hrms.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * Find employee by email
     */
    Optional<Employee> findByEmail(String email);
    
    /**
     * Find employees by department id
     */
    List<Employee> findByDepartmentId(Long departmentId);
    
    /**
     * Find employees by department name
     */
    @Query("SELECT e FROM Employee e JOIN e.department d WHERE d.name = :departmentName")
    List<Employee> findByDepartmentName(@Param("departmentName") String departmentName);
    
    /**
     * Find employees by position
     */
    List<Employee> findByPosition(String position);
    
    /**
     * Find employees by name containing (case-insensitive)
     */
    List<Employee> findByNameContainingIgnoreCase(String name);
    
    /**
     * Find employees by position containing (case-insensitive)
     */
    List<Employee> findByPositionContainingIgnoreCase(String position);
    
    /**
     * Find employees by salary range
     */
    @Query("SELECT e FROM Employee e WHERE e.salary BETWEEN :minSalary AND :maxSalary")
    List<Employee> findBySalaryBetween(@Param("minSalary") BigDecimal minSalary, 
                                      @Param("maxSalary") BigDecimal maxSalary);
    
    /**
     * Find employees who joined after a certain date
     */
    List<Employee> findByDateOfJoiningAfter(LocalDate date);
    
    /**
     * Find employees who joined before a certain date
     */
    List<Employee> findByDateOfJoiningBefore(LocalDate date);
    
    /**
     * Find employees who joined between two dates
     */
    List<Employee> findByDateOfJoiningBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Check if employee exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all employees ordered by name
     */
    List<Employee> findAllByOrderByNameAsc();
    
    /**
     * Find all employees ordered by date of joining descending
     */
    List<Employee> findAllByOrderByDateOfJoiningDesc();
    
    /**
     * Find all employees ordered by salary descending
     */
    List<Employee> findAllByOrderBySalaryDesc();
    
    /**
     * Get employee with department details
     */
    @Query("SELECT e FROM Employee e JOIN FETCH e.department WHERE e.id = :id")
    Optional<Employee> findByIdWithDepartment(@Param("id") Long id);
    
    /**
     * Search employees by multiple criteria
     */
    @Query("SELECT e FROM Employee e JOIN e.department d WHERE " +
           "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:position IS NULL OR LOWER(e.position) LIKE LOWER(CONCAT('%', :position, '%'))) AND " +
           "(:departmentName IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :departmentName, '%')))")
    List<Employee> searchEmployees(@Param("name") String name,
                                  @Param("position") String position,
                                  @Param("departmentName") String departmentName);
    
    /**
     * Get employee count by department
     */
    @Query("SELECT d.name, COUNT(e) FROM Employee e JOIN e.department d GROUP BY d.name")
    List<Object[]> getEmployeeCountByDepartment();
}