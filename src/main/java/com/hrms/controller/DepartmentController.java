package com.hrms.controller;

import com.hrms.config.SwaggerResponses;
import com.hrms.dto.ApiResponse;
import com.hrms.dto.DepartmentDTO;
import com.hrms.entity.Department;
import com.hrms.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@Tag(name = "Department Management", description = "APIs for managing departments")
public class DepartmentController {
    
    private final DepartmentService departmentService;
    
    @Autowired
    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }
    
    @GetMapping
    @Operation(summary = "Get all departments", description = "Retrieve all departments ordered by name")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getAllDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartmentsAsDTO();
        return ResponseEntity.ok(ApiResponse.success("Departments retrieved successfully", departments));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get department by ID", description = "Retrieve a specific department by its ID with employee count (employees list not included)")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartmentById(
            @Parameter(description = "Department ID", required = true) @PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentByIdAsDTO(id);
        return ResponseEntity.ok(ApiResponse.success("Department retrieved successfully", department));
    }
    
    @GetMapping("/name/{name}")
    @Operation(summary = "Get department by name", description = "Retrieve a department by its name")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<Department>> getDepartmentByName(
            @Parameter(description = "Department name", required = true) @PathVariable String name) {
        Department department = departmentService.getDepartmentByName(name);
        return ResponseEntity.ok(ApiResponse.success("Department retrieved successfully", department));
    }
    
    @PostMapping
    @Operation(summary = "Create new department", description = "Create a new department")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<Department>> createDepartment(
            @Parameter(description = "Department details", required = true) 
            @Valid @RequestBody Department department) {
        Department createdDepartment = departmentService.createDepartment(department);
        return new ResponseEntity<>(
            ApiResponse.success("Department created successfully", createdDepartment), 
            HttpStatus.CREATED
        );
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update department", description = "Update an existing department")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<Department>> updateDepartment(
            @Parameter(description = "Department ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated department details", required = true) 
            @Valid @RequestBody Department department) {
        Department updatedDepartment = departmentService.updateDepartment(id, department);
        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updatedDepartment));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete department", description = "Delete a department by ID")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<Object>> deleteDepartment(
            @Parameter(description = "Department ID", required = true) @PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully", null));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search departments", description = "Search departments by name (case-insensitive, returns department info with employee count)")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> searchDepartments(
            @Parameter(description = "Department name to search", required = true) 
            @RequestParam String name) {
        List<DepartmentDTO> departments = departmentService.searchDepartmentsByNameAsDTO(name);
        return ResponseEntity.ok(ApiResponse.success("Search completed successfully", departments));
    }
    
    @GetMapping("/{id}/with-employees")
    @Operation(summary = "Get department with employees", description = "Retrieve a department with its employees (structured to prevent circular references)")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartmentWithEmployees(
            @Parameter(description = "Department ID", required = true) @PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentWithEmployeesAsDTO(id);
        return ResponseEntity.ok(ApiResponse.success("Department with employees retrieved successfully", department));
    }
    
    @GetMapping("/employee-count")
    @Operation(summary = "Get departments with employee count", description = "Retrieve all departments with their employee counts")
    @SwaggerResponses.CrudResponses
    public ResponseEntity<ApiResponse<List<Object[]>>> getDepartmentsWithEmployeeCount() {
        List<Object[]> stats = departmentService.getDepartmentsWithEmployeeCount();
        return ResponseEntity.ok(ApiResponse.success("Department statistics retrieved successfully", stats));
    }
}