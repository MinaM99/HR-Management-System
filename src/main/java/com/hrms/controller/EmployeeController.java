package com.hrms.controller;

import com.hrms.dto.ApiResponse;
import com.hrms.entity.Employee;
import com.hrms.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "APIs for managing employees")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    
    @GetMapping
    @Operation(summary = "Get all employees", description = "Retrieve all employees ordered by name")
    public ResponseEntity<ApiResponse<List<Employee>>> getAllEmployees() {
        List<Employee> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Retrieve a specific employee by ID")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeById(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", employee));
    }
    
    @GetMapping("/email/{email}")
    @Operation(summary = "Get employee by email", description = "Retrieve an employee by email address")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeByEmail(
            @Parameter(description = "Employee email", required = true) @PathVariable String email) {
        Employee employee = employeeService.getEmployeeByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Employee retrieved successfully", employee));
    }
    
    @PostMapping
    @Operation(summary = "Create new employee", description = "Create a new employee record")
    public ResponseEntity<ApiResponse<Employee>> createEmployee(
            @Parameter(description = "Employee details", required = true) 
            @Valid @RequestBody Employee employee) {
        Employee createdEmployee = employeeService.createEmployee(employee);
        return new ResponseEntity<>(
            ApiResponse.success("Employee created successfully", createdEmployee), 
            HttpStatus.CREATED
        );
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update employee", description = "Update an existing employee")
    public ResponseEntity<ApiResponse<Employee>> updateEmployee(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated employee details", required = true) 
            @Valid @RequestBody Employee employee) {
        Employee updatedEmployee = employeeService.updateEmployee(id, employee);
        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updatedEmployee));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee", description = "Delete an employee by ID")
    public ResponseEntity<ApiResponse<Object>> deleteEmployee(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", null));
    }
    
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "Get employees by department", description = "Retrieve employees by department ID")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByDepartment(
            @Parameter(description = "Department ID", required = true) @PathVariable Long departmentId) {
        List<Employee> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }
    
    @GetMapping("/department/name/{departmentName}")
    @Operation(summary = "Get employees by department name", description = "Retrieve employees by department name")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByDepartmentName(
            @Parameter(description = "Department name", required = true) @PathVariable String departmentName) {
        List<Employee> employees = employeeService.getEmployeesByDepartmentName(departmentName);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }
    
    @GetMapping("/position/{position}")
    @Operation(summary = "Get employees by position", description = "Retrieve employees by position")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesByPosition(
            @Parameter(description = "Position", required = true) @PathVariable String position) {
        List<Employee> employees = employeeService.getEmployeesByPosition(position);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }
    
    @GetMapping("/search/name")
    @Operation(summary = "Search employees by name", description = "Search employees by name (case-insensitive)")
    public ResponseEntity<ApiResponse<List<Employee>>> searchEmployeesByName(
            @Parameter(description = "Name to search", required = true) @RequestParam String name) {
        List<Employee> employees = employeeService.searchEmployeesByName(name);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", employees));
    }
    
    @GetMapping("/search/position")
    @Operation(summary = "Search employees by position", description = "Search employees by position (case-insensitive)")
    public ResponseEntity<ApiResponse<List<Employee>>> searchEmployeesByPosition(
            @Parameter(description = "Position to search", required = true) @RequestParam String position) {
        List<Employee> employees = employeeService.searchEmployeesByPosition(position);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", employees));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search employees by multiple criteria", description = "Search employees by name, position, and department")
    public ResponseEntity<ApiResponse<List<Employee>>> searchEmployees(
            @Parameter(description = "Name to search") @RequestParam(required = false) String name,
            @Parameter(description = "Position to search") @RequestParam(required = false) String position,
            @Parameter(description = "Department name to search") @RequestParam(required = false) String departmentName) {
        List<Employee> employees = employeeService.searchEmployees(name, position, departmentName);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", employees));
    }
    
    @GetMapping("/salary-range")
    @Operation(summary = "Get employees by salary range", description = "Retrieve employees within a salary range")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesBySalaryRange(
            @Parameter(description = "Minimum salary", required = true) @RequestParam BigDecimal minSalary,
            @Parameter(description = "Maximum salary", required = true) @RequestParam BigDecimal maxSalary) {
        List<Employee> employees = employeeService.getEmployeesBySalaryRange(minSalary, maxSalary);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }
    
    @GetMapping("/joined-after")
    @Operation(summary = "Get employees who joined after a date", description = "Retrieve employees who joined after a specific date")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesJoinedAfter(
            @Parameter(description = "Date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Employee> employees = employeeService.getEmployeesJoinedAfter(date);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }
    
    @GetMapping("/joined-before")
    @Operation(summary = "Get employees who joined before a date", description = "Retrieve employees who joined before a specific date")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesJoinedBefore(
            @Parameter(description = "Date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Employee> employees = employeeService.getEmployeesJoinedBefore(date);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }
    
    @GetMapping("/joined-between")
    @Operation(summary = "Get employees who joined between dates", description = "Retrieve employees who joined between two dates")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesJoinedBetween(
            @Parameter(description = "Start date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Employee> employees = employeeService.getEmployeesJoinedBetween(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }
    
    @GetMapping("/order-by-salary")
    @Operation(summary = "Get employees ordered by salary", description = "Retrieve employees ordered by salary (highest first)")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesOrderedBySalary() {
        List<Employee> employees = employeeService.getEmployeesOrderedBySalary();
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }
    
    @GetMapping("/order-by-joining-date")
    @Operation(summary = "Get employees ordered by joining date", description = "Retrieve employees ordered by joining date (newest first)")
    public ResponseEntity<ApiResponse<List<Employee>>> getEmployeesOrderedByJoiningDate() {
        List<Employee> employees = employeeService.getEmployeesOrderedByJoiningDate();
        return ResponseEntity.ok(ApiResponse.success("Employees retrieved successfully", employees));
    }
    
    @GetMapping("/{id}/with-department")
    @Operation(summary = "Get employee with department details", description = "Retrieve an employee with department information")
    public ResponseEntity<ApiResponse<Employee>> getEmployeeWithDepartment(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long id) {
        Employee employee = employeeService.getEmployeeWithDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Employee with department retrieved successfully", employee));
    }
    
    @GetMapping("/statistics/by-department")
    @Operation(summary = "Get employee count by department", description = "Get employee count statistics by department")
    public ResponseEntity<ApiResponse<List<Object[]>>> getEmployeeCountByDepartment() {
        List<Object[]> stats = employeeService.getEmployeeCountByDepartment();
        return ResponseEntity.ok(ApiResponse.success("Employee statistics retrieved successfully", stats));
    }
    
    @GetMapping("/{id}/exists")
    @Operation(summary = "Check if employee exists", description = "Check if an employee exists by ID")
    public ResponseEntity<ApiResponse<Boolean>> employeeExists(
            @Parameter(description = "Employee ID", required = true) @PathVariable Long id) {
        boolean exists = employeeService.employeeExists(id);
        return ResponseEntity.ok(ApiResponse.success("Employee existence checked", exists));
    }
    
    @GetMapping("/email-available")
    @Operation(summary = "Check email availability", description = "Check if an email address is available")
    public ResponseEntity<ApiResponse<Boolean>> isEmailAvailable(
            @Parameter(description = "Email to check", required = true) @RequestParam String email) {
        boolean available = employeeService.isEmailAvailable(email);
        return ResponseEntity.ok(ApiResponse.success("Email availability checked", available));
    }
}
