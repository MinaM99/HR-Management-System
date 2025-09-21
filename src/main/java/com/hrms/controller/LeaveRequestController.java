package com.hrms.controller;

import com.hrms.dto.ApiResponse;
import com.hrms.entity.LeaveRequest;
import com.hrms.entity.LeaveRequest.LeaveStatus;
import com.hrms.entity.LeaveRequest.LeaveType;
import com.hrms.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
@Tag(name = "Leave Request Management", description = "APIs for managing leave requests")
public class LeaveRequestController {
    
    private final LeaveRequestService leaveRequestService;
    
    @Autowired
    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }
    
    @GetMapping
    @Operation(summary = "Get all leave requests", description = "Retrieve all leave requests")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = leaveRequestService.getAllLeaveRequests();
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved successfully", leaveRequests));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get leave request by ID", description = "Retrieve a specific leave request by ID")
    public ResponseEntity<ApiResponse<LeaveRequest>> getLeaveRequestById(
            @Parameter(description = "Leave request ID", required = true) @PathVariable Long id) {
        LeaveRequest leaveRequest = leaveRequestService.getLeaveRequestById(id);
        return ResponseEntity.ok(ApiResponse.success("Leave request retrieved successfully", leaveRequest));
    }
    
    @PostMapping
    @Operation(summary = "Create new leave request", description = "Submit a new leave request")
    public ResponseEntity<ApiResponse<LeaveRequest>> createLeaveRequest(
            @Parameter(description = "Leave request details", required = true) 
            @Valid @RequestBody LeaveRequest leaveRequest) {
        LeaveRequest createdLeaveRequest = leaveRequestService.createLeaveRequest(leaveRequest);
        return new ResponseEntity<>(
            ApiResponse.success("Leave request created successfully", createdLeaveRequest), 
            HttpStatus.CREATED
        );
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update leave request", description = "Update an existing leave request")
    public ResponseEntity<ApiResponse<LeaveRequest>> updateLeaveRequest(
            @Parameter(description = "Leave request ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated leave request details", required = true) 
            @Valid @RequestBody LeaveRequest leaveRequest) {
        LeaveRequest updatedLeaveRequest = leaveRequestService.updateLeaveRequest(id, leaveRequest);
        return ResponseEntity.ok(ApiResponse.success("Leave request updated successfully", updatedLeaveRequest));
    }
    
    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve leave request", description = "Approve a pending leave request")
    public ResponseEntity<ApiResponse<LeaveRequest>> approveLeaveRequest(
            @Parameter(description = "Leave request ID", required = true) @PathVariable Long id,
            @Parameter(description = "Admin comments") @RequestParam(required = false) String adminComments) {
        LeaveRequest approvedRequest = leaveRequestService.approveLeaveRequest(id, adminComments);
        return ResponseEntity.ok(ApiResponse.success("Leave request approved successfully", approvedRequest));
    }
    
    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject leave request", description = "Reject a pending leave request")
    public ResponseEntity<ApiResponse<LeaveRequest>> rejectLeaveRequest(
            @Parameter(description = "Leave request ID", required = true) @PathVariable Long id,
            @Parameter(description = "Admin comments") @RequestParam(required = false) String adminComments) {
        LeaveRequest rejectedRequest = leaveRequestService.rejectLeaveRequest(id, adminComments);
        return ResponseEntity.ok(ApiResponse.success("Leave request rejected successfully", rejectedRequest));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete leave request", description = "Delete a leave request by ID")
    public ResponseEntity<ApiResponse<Object>> deleteLeaveRequest(
            @Parameter(description = "Leave request ID", required = true) @PathVariable Long id) {
        leaveRequestService.deleteLeaveRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Leave request deleted successfully", null));
    }
    
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get leave requests by employee", description = "Retrieve leave requests for a specific employee")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getLeaveRequestsByEmployee(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long employeeId) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved successfully", leaveRequests));
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get leave requests by status", description = "Retrieve leave requests by status")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getLeaveRequestsByStatus(
            @Parameter(description = "Leave status", required = true) @PathVariable LeaveStatus status) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved successfully", leaveRequests));
    }
    
    @GetMapping("/pending")
    @Operation(summary = "Get pending leave requests", description = "Retrieve all pending leave requests")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getPendingLeaveRequests() {
        List<LeaveRequest> leaveRequests = leaveRequestService.getPendingLeaveRequests();
        return ResponseEntity.ok(ApiResponse.success("Pending leave requests retrieved successfully", leaveRequests));
    }
    
    @GetMapping("/employee/{employeeId}/status/{status}")
    @Operation(summary = "Get leave requests by employee and status", description = "Retrieve leave requests for an employee with specific status")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getLeaveRequestsByEmployeeAndStatus(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long employeeId,
            @Parameter(description = "Leave status", required = true) @PathVariable LeaveStatus status) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByEmployeeAndStatus(employeeId, status);
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved successfully", leaveRequests));
    }
    
    @GetMapping("/type/{type}")
    @Operation(summary = "Get leave requests by type", description = "Retrieve leave requests by leave type")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getLeaveRequestsByType(
            @Parameter(description = "Leave type", required = true) @PathVariable LeaveType type) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByType(type);
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved successfully", leaveRequests));
    }
    
    @GetMapping("/date-range")
    @Operation(summary = "Get leave requests by date range", description = "Retrieve leave requests within a date range")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getLeaveRequestsByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved successfully", leaveRequests));
    }
    
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get leave requests by department", description = "Retrieve leave requests for a specific department")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getLeaveRequestsByDepartment(
            @Parameter(description = "Department ID", required = true) @PathVariable Long departmentId) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getLeaveRequestsByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Leave requests retrieved successfully", leaveRequests));
    }
    
    @GetMapping("/employee/{employeeId}/year/{year}/approved")
    @Operation(summary = "Get approved leaves by employee and year", description = "Retrieve approved leave requests for an employee in a specific year")
    public ResponseEntity<ApiResponse<List<LeaveRequest>>> getApprovedLeavesByEmployeeAndYear(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long employeeId,
            @Parameter(description = "Year", required = true) @PathVariable int year) {
        List<LeaveRequest> leaveRequests = leaveRequestService.getApprovedLeavesByEmployeeAndYear(employeeId, year);
        return ResponseEntity.ok(ApiResponse.success("Approved leave requests retrieved successfully", leaveRequests));
    }
    
    @GetMapping("/employee/{employeeId}/leave-days-count")
    @Operation(summary = "Get approved leave days count", description = "Get count of approved leave days for an employee in a specific month")
    public ResponseEntity<ApiResponse<Long>> getApprovedLeaveDaysCount(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long employeeId,
            @Parameter(description = "Year", required = true) @RequestParam int year,
            @Parameter(description = "Month", required = true) @RequestParam int month) {
        Long count = leaveRequestService.getApprovedLeaveDaysCount(employeeId, year, month);
        return ResponseEntity.ok(ApiResponse.success("Leave days count retrieved successfully", count));
    }
    
    @GetMapping("/statistics")
    @Operation(summary = "Get leave statistics", description = "Get leave request statistics by type and status")
    public ResponseEntity<ApiResponse<List<Object[]>>> getLeaveStatistics() {
        List<Object[]> stats = leaveRequestService.getLeaveStatistics();
        return ResponseEntity.ok(ApiResponse.success("Leave statistics retrieved successfully", stats));
    }
    
    @GetMapping("/calculate-duration")
    @Operation(summary = "Calculate leave duration", description = "Calculate the duration of leave in days")
    public ResponseEntity<ApiResponse<Long>> calculateLeaveDuration(
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        long duration = leaveRequestService.calculateLeaveDuration(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Leave duration calculated successfully", duration));
    }
    
    @GetMapping("/employee/{employeeId}/has-overlapping")
    @Operation(summary = "Check for overlapping leaves", description = "Check if employee has overlapping leaves for given dates")
    public ResponseEntity<ApiResponse<Boolean>> hasOverlappingLeaves(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long employeeId,
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Request ID to exclude from check") 
            @RequestParam(required = false) Long excludeRequestId) {
        boolean hasOverlapping = leaveRequestService.hasOverlappingLeaves(employeeId, startDate, endDate, excludeRequestId);
        return ResponseEntity.ok(ApiResponse.success("Overlap check completed", hasOverlapping));
    }
}
