package com.hrms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Department entity
 * Flexible DTO that can be used with or without employee details
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Only include non-null fields in JSON response
public class DepartmentDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer employeeCount;
    private List<EmployeeDTO> employees;

    // Constructors
    public DepartmentDTO() {
    }

    public DepartmentDTO(Long id, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Convenience constructor with employee count
    public DepartmentDTO(Long id, String name, String description, LocalDateTime createdAt, LocalDateTime updatedAt, Integer employeeCount) {
        this(id, name, description, createdAt, updatedAt);
        this.employeeCount = employeeCount;
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

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public List<EmployeeDTO> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeDTO> employees) {
        this.employees = employees;
        // Automatically set employee count when employees list is set
        this.employeeCount = employees != null ? employees.size() : 0;
    }

    // Utility methods for cleaner API usage
    public boolean hasEmployees() {
        return employees != null && !employees.isEmpty();
    }

    public DepartmentDTO withEmployees(List<EmployeeDTO> employees) {
        setEmployees(employees);
        return this;
    }

    public DepartmentDTO withEmployeeCount(Integer count) {
        setEmployeeCount(count);
        return this;
    }
}