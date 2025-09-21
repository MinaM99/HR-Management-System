package com.hrms.service;

import com.hrms.entity.LeaveRequest;
import com.hrms.entity.LeaveRequest.LeaveStatus;
import com.hrms.entity.LeaveRequest.LeaveType;
import com.hrms.entity.Employee;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.exception.BadRequestException;
import com.hrms.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class LeaveRequestService {
    
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeService employeeService;
    
    @Autowired
    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository, EmployeeService employeeService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.employeeService = employeeService;
    }
    
    /**
     * Get all leave requests
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveRequestRepository.findAll();
    }
    
    /**
     * Get leave request by id
     */
    @Transactional(readOnly = true)
    public LeaveRequest getLeaveRequestById(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave Request", "id", id));
    }
    
    /**
     * Create new leave request
     */
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        // Validate employee exists
        Employee employee = employeeService.getEmployeeById(leaveRequest.getEmployee().getId());
        leaveRequest.setEmployee(employee);
        
        // Validate dates
        validateLeaveDates(leaveRequest.getStartDate(), leaveRequest.getEndDate());
        
        // Check for overlapping leave requests
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingLeaveRequests(
                employee.getId(), 
                leaveRequest.getStartDate(), 
                leaveRequest.getEndDate(),
                0L // New request, so no ID to exclude
        );
        
        if (!overlappingRequests.isEmpty()) {
            throw new BadRequestException("Leave request overlaps with existing leave requests");
        }
        
        // Set default status if not provided
        if (leaveRequest.getStatus() == null) {
            leaveRequest.setStatus(LeaveStatus.PENDING);
        }
        
        return leaveRequestRepository.save(leaveRequest);
    }
    
    /**
     * Update leave request
     */
    public LeaveRequest updateLeaveRequest(Long id, LeaveRequest leaveRequestDetails) {
        LeaveRequest leaveRequest = getLeaveRequestById(id);
        
        // Validate dates
        validateLeaveDates(leaveRequestDetails.getStartDate(), leaveRequestDetails.getEndDate());
        
        // Check for overlapping leave requests (excluding current request)
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingLeaveRequests(
                leaveRequest.getEmployee().getId(),
                leaveRequestDetails.getStartDate(),
                leaveRequestDetails.getEndDate(),
                id
        );
        
        if (!overlappingRequests.isEmpty()) {
            throw new BadRequestException("Leave request overlaps with existing leave requests");
        }
        
        // Update leave request details
        leaveRequest.setStartDate(leaveRequestDetails.getStartDate());
        leaveRequest.setEndDate(leaveRequestDetails.getEndDate());
        leaveRequest.setLeaveType(leaveRequestDetails.getLeaveType());
        leaveRequest.setReason(leaveRequestDetails.getReason());
        
        // Only allow status update if it's provided
        if (leaveRequestDetails.getStatus() != null) {
            leaveRequest.setStatus(leaveRequestDetails.getStatus());
        }
        
        // Only allow admin comments update if it's provided
        if (leaveRequestDetails.getAdminComments() != null) {
            leaveRequest.setAdminComments(leaveRequestDetails.getAdminComments());
        }
        
        return leaveRequestRepository.save(leaveRequest);
    }
    
    /**
     * Approve leave request
     */
    public LeaveRequest approveLeaveRequest(Long id, String adminComments) {
        LeaveRequest leaveRequest = getLeaveRequestById(id);
        
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be approved");
        }
        
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        if (adminComments != null && !adminComments.trim().isEmpty()) {
            leaveRequest.setAdminComments(adminComments);
        }
        
        return leaveRequestRepository.save(leaveRequest);
    }
    
    /**
     * Reject leave request
     */
    public LeaveRequest rejectLeaveRequest(Long id, String adminComments) {
        LeaveRequest leaveRequest = getLeaveRequestById(id);
        
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending leave requests can be rejected");
        }
        
        leaveRequest.setStatus(LeaveStatus.REJECTED);
        if (adminComments != null && !adminComments.trim().isEmpty()) {
            leaveRequest.setAdminComments(adminComments);
        }
        
        return leaveRequestRepository.save(leaveRequest);
    }
    
    /**
     * Delete leave request
     */
    public void deleteLeaveRequest(Long id) {
        LeaveRequest leaveRequest = getLeaveRequestById(id);
        leaveRequestRepository.delete(leaveRequest);
    }
    
    /**
     * Get leave requests by employee
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByStartDateDesc(employeeId);
    }
    
    /**
     * Get leave requests by status
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByStatus(LeaveStatus status) {
        return leaveRequestRepository.findByStatusOrderByCreatedAtAsc(status);
    }
    
    /**
     * Get pending leave requests
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatusOrderByCreatedAtAsc(LeaveStatus.PENDING);
    }
    
    /**
     * Get leave requests by employee and status
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByEmployeeAndStatus(Long employeeId, LeaveStatus status) {
        return leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, status);
    }
    
    /**
     * Get leave requests by leave type
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByType(LeaveType leaveType) {
        return leaveRequestRepository.findByLeaveType(leaveType);
    }
    
    /**
     * Get leave requests by date range
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByDateRange(LocalDate startDate, LocalDate endDate) {
        validateLeaveDates(startDate, endDate);
        return leaveRequestRepository.findByDateRange(startDate, endDate);
    }
    
    /**
     * Get leave requests by department
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByDepartment(Long departmentId) {
        return leaveRequestRepository.findByDepartmentId(departmentId);
    }
    
    /**
     * Get approved leaves for employee in a year
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getApprovedLeavesByEmployeeAndYear(Long employeeId, int year) {
        return leaveRequestRepository.findApprovedLeavesByEmployeeAndYear(employeeId, year);
    }
    
    /**
     * Get approved leave days count for employee in a month
     */
    @Transactional(readOnly = true)
    public Long getApprovedLeaveDaysCount(Long employeeId, int year, int month) {
        return leaveRequestRepository.countApprovedLeaveDaysByEmployeeAndMonth(employeeId, year, month);
    }
    
    /**
     * Get leave statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getLeaveStatistics() {
        return leaveRequestRepository.getLeaveStatistics();
    }
    
    /**
     * Calculate leave duration in days
     */
    public long calculateLeaveDuration(LocalDate startDate, LocalDate endDate) {
        validateLeaveDates(startDate, endDate);
        return startDate.until(endDate).getDays() + 1;
    }
    
    /**
     * Check if employee has overlapping leaves
     */
    @Transactional(readOnly = true)
    public boolean hasOverlappingLeaves(Long employeeId, LocalDate startDate, LocalDate endDate, Long excludeRequestId) {
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findOverlappingLeaveRequests(
                employeeId, startDate, endDate, excludeRequestId != null ? excludeRequestId : 0L
        );
        return !overlappingRequests.isEmpty();
    }
    
    /**
     * Validate leave dates
     */
    private void validateLeaveDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("Start date and end date are required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        
        if (startDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Leave cannot be requested for past dates");
        }
    }
}