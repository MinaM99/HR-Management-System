package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Start date is required")
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Leave type is required")
    @Column(name = "leave_type", nullable = false)
    private LeaveType leaveType;
    
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    @Column(name = "reason")
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeaveStatus status = LeaveStatus.PENDING;
    
    @Size(max = 500, message = "Admin comments cannot exceed 500 characters")
    @Column(name = "admin_comments")
    private String adminComments;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "Employee is required")
    @JsonBackReference
    private Employee employee;
    
    // Enums
    public enum LeaveType {
        SICK, VACATION, PERSONAL, MATERNITY, PATERNITY, EMERGENCY, OTHER
    }
    
    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED
    }
    
    // Constructors
    public LeaveRequest() {
    }
    
    public LeaveRequest(LocalDate startDate, LocalDate endDate, LeaveType leaveType, String reason, Employee employee) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.leaveType = leaveType;
        this.reason = reason;
        this.employee = employee;
        this.status = LeaveStatus.PENDING;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = LeaveStatus.PENDING;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public long getDurationInDays() {
        return startDate.until(endDate).getDays() + 1;
    }
    
    public boolean isApproved() {
        return status == LeaveStatus.APPROVED;
    }
    
    public boolean isPending() {
        return status == LeaveStatus.PENDING;
    }
    
    public boolean isRejected() {
        return status == LeaveStatus.REJECTED;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public LeaveType getLeaveType() {
        return leaveType;
    }
    
    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public LeaveStatus getStatus() {
        return status;
    }
    
    public void setStatus(LeaveStatus status) {
        this.status = status;
    }
    
    public String getAdminComments() {
        return adminComments;
    }
    
    public void setAdminComments(String adminComments) {
        this.adminComments = adminComments;
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
    
    public Employee getEmployee() {
        return employee;
    }
    
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    
    @Override
    public String toString() {
        return "LeaveRequest{" +
                "id=" + id +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", leaveType=" + leaveType +
                ", reason='" + reason + '\'' +
                ", status=" + status +
                ", adminComments='" + adminComments + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}