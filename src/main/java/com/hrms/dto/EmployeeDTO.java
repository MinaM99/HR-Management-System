package com.hrms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Employee entity
 * Flexible DTO that can be used for various employee API responses
 * Uses Jackson annotations to optimize JSON serialization
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields in JSON response
public class EmployeeDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String position;
    private LocalDate dateOfJoining;
    private BigDecimal salary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Department information (basic to prevent circular reference)
    private DepartmentBasicInfo department;
    
    // Statistics (optional fields)
    private Integer totalLeaveRequests;
    private Integer pendingLeaveRequests;
    private Integer totalPayrollRecords;

    // Constructors
    public EmployeeDTO() {
    }

    // Basic constructor for essential fields
    public EmployeeDTO(Long id, String name, String email, String position, LocalDate dateOfJoining, BigDecimal salary) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.position = position;
        this.dateOfJoining = dateOfJoining;
        this.salary = salary;
    }

    // Full constructor
    public EmployeeDTO(Long id, String name, String email, String phone, String position, 
                      LocalDate dateOfJoining, BigDecimal salary, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.position = position;
        this.dateOfJoining = dateOfJoining;
        this.salary = salary;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
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

    public DepartmentBasicInfo getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentBasicInfo department) {
        this.department = department;
    }

    public Integer getTotalLeaveRequests() {
        return totalLeaveRequests;
    }

    public void setTotalLeaveRequests(Integer totalLeaveRequests) {
        this.totalLeaveRequests = totalLeaveRequests;
    }

    public Integer getPendingLeaveRequests() {
        return pendingLeaveRequests;
    }

    public void setPendingLeaveRequests(Integer pendingLeaveRequests) {
        this.pendingLeaveRequests = pendingLeaveRequests;
    }

    public Integer getTotalPayrollRecords() {
        return totalPayrollRecords;
    }

    public void setTotalPayrollRecords(Integer totalPayrollRecords) {
        this.totalPayrollRecords = totalPayrollRecords;
    }

    // Utility methods for fluent API
    public EmployeeDTO withDepartment(DepartmentBasicInfo department) {
        setDepartment(department);
        return this;
    }

    public EmployeeDTO withStatistics(Integer totalLeaveRequests, Integer pendingLeaveRequests, Integer totalPayrollRecords) {
        setTotalLeaveRequests(totalLeaveRequests);
        setPendingLeaveRequests(pendingLeaveRequests);
        setTotalPayrollRecords(totalPayrollRecords);
        return this;
    }

    public EmployeeDTO withPhone(String phone) {
        setPhone(phone);
        return this;
    }

    public EmployeeDTO withTimestamps(LocalDateTime createdAt, LocalDateTime updatedAt) {
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
        return this;
    }

    // Inner class for basic department info to prevent circular references
    public static class DepartmentBasicInfo {
        private Long id;
        private String name;
        private String description;

        public DepartmentBasicInfo() {
        }

        public DepartmentBasicInfo(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        // Getters and Setters
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
    }
}