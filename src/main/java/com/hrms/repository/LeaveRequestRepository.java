package com.hrms.repository;

import com.hrms.entity.LeaveRequest;
import com.hrms.entity.LeaveRequest.LeaveStatus;
import com.hrms.entity.LeaveRequest.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    
    /**
     * Find leave requests by employee id
     */
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    
    /**
     * Find leave requests by employee id and status
     */
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
    
    /**
     * Find leave requests by status
     */
    List<LeaveRequest> findByStatus(LeaveStatus status);
    
    /**
     * Find leave requests by leave type
     */
    List<LeaveRequest> findByLeaveType(LeaveType leaveType);
    
    /**
     * Find leave requests by employee and date range
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND " +
           "((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    /**
     * Find leave requests by date range
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findByDateRange(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);
    
    /**
     * Find pending leave requests ordered by creation date
     */
    List<LeaveRequest> findByStatusOrderByCreatedAtAsc(LeaveStatus status);
    
    /**
     * Find leave requests by employee ordered by start date descending
     */
    List<LeaveRequest> findByEmployeeIdOrderByStartDateDesc(Long employeeId);
    
    /**
     * Find approved leave requests for an employee in a year
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND " +
           "lr.status = 'APPROVED' AND YEAR(lr.startDate) = :year")
    List<LeaveRequest> findApprovedLeavesByEmployeeAndYear(@Param("employeeId") Long employeeId,
                                                          @Param("year") int year);
    
    /**
     * Count approved leave days for an employee in a month
     */
    @Query("SELECT COALESCE(SUM(DATEDIFF(lr.endDate, lr.startDate) + 1), 0) FROM LeaveRequest lr " +
           "WHERE lr.employee.id = :employeeId AND lr.status = 'APPROVED' AND " +
           "YEAR(lr.startDate) = :year AND MONTH(lr.startDate) = :month")
    Long countApprovedLeaveDaysByEmployeeAndMonth(@Param("employeeId") Long employeeId,
                                                 @Param("year") int year,
                                                 @Param("month") int month);
    
    /**
     * Find overlapping leave requests for an employee (excluding current request)
     */
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND " +
           "lr.id != :excludeId AND lr.status != 'REJECTED' AND " +
           "((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaveRequests(@Param("employeeId") Long employeeId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate,
                                                   @Param("excludeId") Long excludeId);
    
    /**
     * Get leave statistics by type and status
     */
    @Query("SELECT lr.leaveType, lr.status, COUNT(lr) FROM LeaveRequest lr GROUP BY lr.leaveType, lr.status")
    List<Object[]> getLeaveStatistics();
    
    /**
     * Find leave requests by department
     */
    @Query("SELECT lr FROM LeaveRequest lr JOIN lr.employee e JOIN e.department d WHERE d.id = :departmentId")
    List<LeaveRequest> findByDepartmentId(@Param("departmentId") Long departmentId);
    
    /**
     * Find leave requests created between dates
     */
    List<LeaveRequest> findByCreatedAtBetween(LocalDate startDate, LocalDate endDate);
}