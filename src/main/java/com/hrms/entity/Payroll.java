package com.hrms.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payrolls", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "month", "year"}))
public class Payroll {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    @Column(name = "month", nullable = false)
    private Integer month;
    
    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    @Column(name = "year", nullable = false)
    private Integer year;
    
    @NotNull(message = "Total salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total salary must be positive")
    @Column(name = "total_salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalSalary;
    
    @DecimalMin(value = "0.0", message = "Deductions cannot be negative")
    @Column(name = "deductions", precision = 10, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Bonuses cannot be negative")
    @Column(name = "bonuses", precision = 10, scale = 2)
    private BigDecimal bonuses = BigDecimal.ZERO;
    
    @Column(name = "net_pay", precision = 10, scale = 2)
    private BigDecimal netPay;
    
    @Column(name = "working_days")
    private Integer workingDays;
    
    @Column(name = "leave_days_taken")
    private Integer leaveDaysTaken = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id", nullable = false)
    @NotNull(message = "Employee is required")
    @JsonBackReference
    private Employee employee;
    
    // Constructors
    public Payroll() {
    }
    
    public Payroll(Integer month, Integer year, BigDecimal totalSalary, Employee employee) {
        this.month = month;
        this.year = year;
        this.totalSalary = totalSalary;
        this.employee = employee;
        this.deductions = BigDecimal.ZERO;
        this.bonuses = BigDecimal.ZERO;
        this.leaveDaysTaken = 0;
        calculateNetPay();
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateNetPay();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateNetPay();
    }
    
    // Business methods
    public void calculateNetPay() {
        if (totalSalary != null) {
            netPay = totalSalary
                    .add(bonuses != null ? bonuses : BigDecimal.ZERO)
                    .subtract(deductions != null ? deductions : BigDecimal.ZERO);
        }
    }
    
    public String getPayrollPeriod() {
        return month + "/" + year;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Integer getMonth() {
        return month;
    }
    
    public void setMonth(Integer month) {
        this.month = month;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public BigDecimal getTotalSalary() {
        return totalSalary;
    }
    
    public void setTotalSalary(BigDecimal totalSalary) {
        this.totalSalary = totalSalary;
        calculateNetPay();
    }
    
    public BigDecimal getDeductions() {
        return deductions;
    }
    
    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
        calculateNetPay();
    }
    
    public BigDecimal getBonuses() {
        return bonuses;
    }
    
    public void setBonuses(BigDecimal bonuses) {
        this.bonuses = bonuses;
        calculateNetPay();
    }
    
    public BigDecimal getNetPay() {
        return netPay;
    }
    
    public void setNetPay(BigDecimal netPay) {
        this.netPay = netPay;
    }
    
    public Integer getWorkingDays() {
        return workingDays;
    }
    
    public void setWorkingDays(Integer workingDays) {
        this.workingDays = workingDays;
    }
    
    public Integer getLeaveDaysTaken() {
        return leaveDaysTaken;
    }
    
    public void setLeaveDaysTaken(Integer leaveDaysTaken) {
        this.leaveDaysTaken = leaveDaysTaken;
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
        return "Payroll{" +
                "id=" + id +
                ", month=" + month +
                ", year=" + year +
                ", totalSalary=" + totalSalary +
                ", deductions=" + deductions +
                ", bonuses=" + bonuses +
                ", netPay=" + netPay +
                ", workingDays=" + workingDays +
                ", leaveDaysTaken=" + leaveDaysTaken +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}