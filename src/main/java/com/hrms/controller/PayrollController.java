package com.hrms.controller;

import com.hrms.config.SwaggerResponses;
import com.hrms.dto.ApiResponse;
import com.hrms.entity.Payroll;
import com.hrms.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@Tag(name = "Payroll Management", description = "APIs for managing payroll records")
public class PayrollController {
    
    private final PayrollService payrollService;
    
    @Autowired
    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }
    
    @GetMapping
    @Operation(summary = "Get all payroll records", description = "Retrieve all payroll records ordered by recent first")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<List<Payroll>>> getAllPayrolls() {
        List<Payroll> payrolls = payrollService.getAllPayrolls();
        return ResponseEntity.ok(ApiResponse.success("Payroll records retrieved successfully", payrolls));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get payroll by ID", description = "Retrieve a specific payroll record by ID")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<Payroll>> getPayrollById(
            @Parameter(description = "Payroll ID", required = true) @PathVariable Long id) {
        Payroll payroll = payrollService.getPayrollById(id);
        return ResponseEntity.ok(ApiResponse.success("Payroll record retrieved successfully", payroll));
    }
    
    @PostMapping
    @Operation(summary = "Create new payroll record", description = "Create a new payroll record")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<Payroll>> createPayroll(
            @Parameter(description = "Payroll details", required = true) 
            @Valid @RequestBody Payroll payroll) {
        Payroll createdPayroll = payrollService.createPayroll(payroll);
        return new ResponseEntity<>(
            ApiResponse.success("Payroll record created successfully", createdPayroll), 
            HttpStatus.CREATED
        );
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update payroll record", description = "Update an existing payroll record")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<Payroll>> updatePayroll(
            @Parameter(description = "Payroll ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated payroll details", required = true) 
            @Valid @RequestBody Payroll payroll) {
        Payroll updatedPayroll = payrollService.updatePayroll(id, payroll);
        return ResponseEntity.ok(ApiResponse.success("Payroll record updated successfully", updatedPayroll));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete payroll record", description = "Delete a payroll record by ID")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<Object>> deletePayroll(
            @Parameter(description = "Payroll ID", required = true) @PathVariable Long id) {
        payrollService.deletePayroll(id);
        return ResponseEntity.ok(ApiResponse.success("Payroll record deleted successfully", null));
    }
    
    @PostMapping("/employee/{employeeId}/generate")
    @Operation(summary = "Generate payroll for employee", description = "Generate payroll record for an employee for a specific month and year")
    public ResponseEntity<ApiResponse<Payroll>> generatePayrollForEmployee(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long employeeId,
            @Parameter(description = "Month (1-12)", required = true) @RequestParam Integer month,
            @Parameter(description = "Year", required = true) @RequestParam Integer year) {
        Payroll payroll = payrollService.generatePayrollForEmployee(employeeId, month, year);
        return new ResponseEntity<>(
            ApiResponse.success("Payroll generated successfully", payroll), 
            HttpStatus.CREATED
        );
    }
    
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get payroll records by employee", description = "Retrieve all payroll records for a specific employee")
    public ResponseEntity<ApiResponse<List<Payroll>>> getPayrollsByEmployee(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long employeeId) {
        List<Payroll> payrolls = payrollService.getPayrollsByEmployee(employeeId);
        return ResponseEntity.ok(ApiResponse.success("Payroll records retrieved successfully", payrolls));
    }
    
    @GetMapping("/employee/{employeeId}/period")
    @Operation(summary = "Get payroll by employee and period", description = "Retrieve payroll record for an employee for specific month and year")
    public ResponseEntity<ApiResponse<Payroll>> getPayrollByEmployeeAndPeriod(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long employeeId,
            @Parameter(description = "Month (1-12)", required = true) @RequestParam Integer month,
            @Parameter(description = "Year", required = true) @RequestParam Integer year) {
        Payroll payroll = payrollService.getPayrollByEmployeeAndPeriod(employeeId, month, year);
        return ResponseEntity.ok(ApiResponse.success("Payroll record retrieved successfully", payroll));
    }
    
    @GetMapping("/period")
    @Operation(summary = "Get payroll records by period", description = "Retrieve payroll records for a specific month and year")
    public ResponseEntity<ApiResponse<List<Payroll>>> getPayrollsByPeriod(
            @Parameter(description = "Month (1-12)", required = true) @RequestParam Integer month,
            @Parameter(description = "Year", required = true) @RequestParam Integer year) {
        List<Payroll> payrolls = payrollService.getPayrollsByPeriod(month, year);
        return ResponseEntity.ok(ApiResponse.success("Payroll records retrieved successfully", payrolls));
    }
    
    @GetMapping("/year/{year}")
    @Operation(summary = "Get payroll records by year", description = "Retrieve payroll records for a specific year")
    public ResponseEntity<ApiResponse<List<Payroll>>> getPayrollsByYear(
            @Parameter(description = "Year", required = true) @PathVariable Integer year) {
        List<Payroll> payrolls = payrollService.getPayrollsByYear(year);
        return ResponseEntity.ok(ApiResponse.success("Payroll records retrieved successfully", payrolls));
    }
    
    @GetMapping("/employee/{employeeId}/year/{year}")
    @Operation(summary = "Get payroll records by employee and year", description = "Retrieve payroll records for an employee for a specific year")
    public ResponseEntity<ApiResponse<List<Payroll>>> getPayrollsByEmployeeAndYear(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long employeeId,
            @Parameter(description = "Year", required = true) @PathVariable Integer year) {
        List<Payroll> payrolls = payrollService.getPayrollsByEmployeeAndYear(employeeId, year);
        return ResponseEntity.ok(ApiResponse.success("Payroll records retrieved successfully", payrolls));
    }
    
    @GetMapping("/department/{departmentId}/period")
    @Operation(summary = "Get payroll records by department and period", description = "Retrieve payroll records for a department for specific month and year")
    public ResponseEntity<ApiResponse<List<Payroll>>> getPayrollsByDepartmentAndPeriod(
            @Parameter(description = "Department ID", required = true) @PathVariable Long departmentId,
            @Parameter(description = "Month (1-12)", required = true) @RequestParam Integer month,
            @Parameter(description = "Year", required = true) @RequestParam Integer year) {
        List<Payroll> payrolls = payrollService.getPayrollsByDepartmentAndPeriod(departmentId, month, year);
        return ResponseEntity.ok(ApiResponse.success("Payroll records retrieved successfully", payrolls));
    }
    
    @GetMapping("/reports/total-by-department")
    @Operation(summary = "Get total payroll cost by department", description = "Get total payroll cost by department for a specific period")
    public ResponseEntity<ApiResponse<List<Object[]>>> getTotalPayrollByDepartment(
            @Parameter(description = "Month (1-12)", required = true) @RequestParam Integer month,
            @Parameter(description = "Year", required = true) @RequestParam Integer year) {
        List<Object[]> report = payrollService.getTotalPayrollByDepartment(month, year);
        return ResponseEntity.ok(ApiResponse.success("Payroll report generated successfully", report));
    }
    
    @GetMapping("/reports/total-cost")
    @Operation(summary = "Get total payroll cost", description = "Get total payroll cost for a specific period")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalPayrollCost(
            @Parameter(description = "Month (1-12)", required = true) @RequestParam Integer month,
            @Parameter(description = "Year", required = true) @RequestParam Integer year) {
        BigDecimal totalCost = payrollService.getTotalPayrollCost(month, year);
        return ResponseEntity.ok(ApiResponse.success("Total payroll cost retrieved successfully", totalCost));
    }
    
    @GetMapping("/reports/average-salary-by-department")
    @Operation(summary = "Get average salary by department", description = "Get average salary statistics by department")
    public ResponseEntity<ApiResponse<List<Object[]>>> getAverageSalaryByDepartment() {
        List<Object[]> report = payrollService.getAverageSalaryByDepartment();
        return ResponseEntity.ok(ApiResponse.success("Average salary report generated successfully", report));
    }
    
    @GetMapping("/reports/statistics/year/{year}")
    @Operation(summary = "Get payroll statistics by year", description = "Get comprehensive payroll statistics for a year")
    public ResponseEntity<ApiResponse<Object[]>> getPayrollStatisticsByYear(
            @Parameter(description = "Year", required = true) @PathVariable Integer year) {
        Object[] stats = payrollService.getPayrollStatisticsByYear(year);
        return ResponseEntity.ok(ApiResponse.success("Payroll statistics retrieved successfully", stats));
    }
    
    @GetMapping("/reports/monthly-trends/year/{year}")
    @Operation(summary = "Get monthly payroll trends", description = "Get monthly payroll trends for a year")
    public ResponseEntity<ApiResponse<List<Object[]>>> getMonthlyPayrollTrends(
            @Parameter(description = "Year", required = true) @PathVariable Integer year) {
        List<Object[]> trends = payrollService.getMonthlyPayrollTrends(year);
        return ResponseEntity.ok(ApiResponse.success("Monthly payroll trends retrieved successfully", trends));
    }
    
    @GetMapping("/net-pay-range")
    @Operation(summary = "Get payrolls by net pay range", description = "Retrieve payroll records within a net pay range")
    public ResponseEntity<ApiResponse<List<Payroll>>> getPayrollsByNetPayRange(
            @Parameter(description = "Minimum net pay", required = true) @RequestParam BigDecimal minAmount,
            @Parameter(description = "Maximum net pay", required = true) @RequestParam BigDecimal maxAmount) {
        List<Payroll> payrolls = payrollService.getPayrollsByNetPayRange(minAmount, maxAmount);
        return ResponseEntity.ok(ApiResponse.success("Payroll records retrieved successfully", payrolls));
    }
}
