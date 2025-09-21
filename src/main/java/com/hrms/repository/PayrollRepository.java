package com.hrms.repository;

import com.hrms.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    
    /**
     * Find payroll by employee id
     */
    List<Payroll> findByEmployeeId(Long employeeId);
    
    /**
     * Find payroll by employee id, month and year
     */
    Optional<Payroll> findByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
    
    /**
     * Find payroll by month and year
     */
    List<Payroll> findByMonthAndYear(Integer month, Integer year);
    
    /**
     * Find payroll by year
     */
    List<Payroll> findByYear(Integer year);
    
    /**
     * Find payroll by employee id and year
     */
    List<Payroll> findByEmployeeIdAndYear(Long employeeId, Integer year);
    
    /**
     * Find payroll by employee id ordered by year and month descending
     */
    List<Payroll> findByEmployeeIdOrderByYearDescMonthDesc(Long employeeId);
    
    /**
     * Check if payroll exists for employee, month and year
     */
    boolean existsByEmployeeIdAndMonthAndYear(Long employeeId, Integer month, Integer year);
    
    /**
     * Find payroll by department for a specific month and year
     */
    @Query("SELECT p FROM Payroll p JOIN p.employee e JOIN e.department d WHERE d.id = :departmentId AND p.month = :month AND p.year = :year")
    List<Payroll> findByDepartmentIdAndMonthAndYear(@Param("departmentId") Long departmentId,
                                                   @Param("month") Integer month,
                                                   @Param("year") Integer year);
    
    /**
     * Get total payroll cost by department for a specific month and year
     */
    @Query("SELECT d.name, SUM(p.netPay) FROM Payroll p JOIN p.employee e JOIN e.department d WHERE p.month = :month AND p.year = :year GROUP BY d.name")
    List<Object[]> getTotalPayrollByDepartment(@Param("month") Integer month, @Param("year") Integer year);
    
    /**
     * Get total payroll cost for a specific month and year
     */
    @Query("SELECT SUM(p.netPay) FROM Payroll p WHERE p.month = :month AND p.year = :year")
    BigDecimal getTotalPayrollCost(@Param("month") Integer month, @Param("year") Integer year);
    
    /**
     * Get average salary by department
     */
    @Query("SELECT d.name, AVG(p.totalSalary) FROM Payroll p JOIN p.employee e JOIN e.department d GROUP BY d.name")
    List<Object[]> getAverageSalaryByDepartment();
    
    /**
     * Find payroll with net pay above certain amount
     */
    List<Payroll> findByNetPayGreaterThan(BigDecimal amount);
    
    /**
     * Find payroll with net pay below certain amount
     */
    List<Payroll> findByNetPayLessThan(BigDecimal amount);
    
    /**
     * Find payroll with net pay between amounts
     */
    List<Payroll> findByNetPayBetween(BigDecimal minAmount, BigDecimal maxAmount);
    
    /**
     * Get payroll statistics for a year
     */
    @Query("SELECT " +
           "COUNT(p), " +
           "SUM(p.totalSalary), " +
           "AVG(p.totalSalary), " +
           "SUM(p.deductions), " +
           "SUM(p.bonuses), " +
           "SUM(p.netPay) " +
           "FROM Payroll p WHERE p.year = :year")
    Object[] getPayrollStatisticsByYear(@Param("year") Integer year);
    
    /**
     * Get monthly payroll trends for a year
     */
    @Query("SELECT p.month, COUNT(p), SUM(p.netPay) FROM Payroll p WHERE p.year = :year GROUP BY p.month ORDER BY p.month")
    List<Object[]> getMonthlyPayrollTrends(@Param("year") Integer year);
    
    /**
     * Find recent payrolls
     */
    @Query("SELECT p FROM Payroll p ORDER BY p.year DESC, p.month DESC")
    List<Payroll> findRecentPayrolls();
}