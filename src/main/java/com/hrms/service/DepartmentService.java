package com.hrms.service;

import com.hrms.dto.DepartmentDTO;
import com.hrms.dto.EmployeeDTO;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepartmentService {
    
    private final DepartmentRepository departmentRepository;
    
    @Autowired
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }
    
    /**
     * Get all departments
     */
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAllByOrderByNameAsc();
    }
    
    /**
     * Get department by id
     */
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }
    
    /**
     * Get department by name
     */
    @Transactional(readOnly = true)
    public Department getDepartmentByName(String name) {
        return departmentRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "name", name));
    }
    
    /**
     * Create new department
     */
    public Department createDepartment(Department department) {
        // Check if department with same name already exists
        if (departmentRepository.existsByName(department.getName())) {
            throw new DuplicateResourceException("Department", "name", department.getName());
        }
        
        return departmentRepository.save(department);
    }
    
    /**
     * Update existing department
     */
    public Department updateDepartment(Long id, Department departmentDetails) {
        Department department = getDepartmentById(id);
        
        // Check if another department with the same name exists (excluding current one)
        departmentRepository.findByName(departmentDetails.getName())
                .ifPresent(existingDept -> {
                    if (!existingDept.getId().equals(id)) {
                        throw new DuplicateResourceException("Department", "name", departmentDetails.getName());
                    }
                });
        
        department.setName(departmentDetails.getName());
        department.setDescription(departmentDetails.getDescription());
        
        return departmentRepository.save(department);
    }
    
    /**
     * Delete department
     */
    public void deleteDepartment(Long id) {
        Department department = getDepartmentById(id);
        
        // Check if department has employees
        if (department.getEmployees() != null && !department.getEmployees().isEmpty()) {
            throw new RuntimeException("Cannot delete department with existing employees. Please reassign employees first.");
        }
        
        departmentRepository.delete(department);
    }
    
    /**
     * Search departments by name
     */
    @Transactional(readOnly = true)
    public List<Department> searchDepartmentsByName(String name) {
        return departmentRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Get department with employees
     */
    @Transactional(readOnly = true)
    public Department getDepartmentWithEmployees(Long id) {
        return departmentRepository.findByIdWithEmployees(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }
    
    /**
     * Get departments with employee count
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDepartmentsWithEmployeeCount() {
        return departmentRepository.findDepartmentsWithEmployeeCount();
    }
    
    /**
     * Check if department exists
     */
    @Transactional(readOnly = true)
    public boolean departmentExists(Long id) {
        return departmentRepository.existsById(id);
    }
    
    // DTO Methods to prevent circular references
    
    /**
     * Get all departments as DTOs (without employee details for performance)
     */
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getAllDepartmentsAsDTO() {
        List<Department> departments = departmentRepository.findAllByOrderByNameAsc();
        return departments.stream()
                .map(this::convertToDepartmentDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get department by ID as DTO (without employees)
     */
    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentByIdAsDTO(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return convertToDepartmentDTO(department);
    }
    
    /**
     * Get department with employees as DTO
     */
    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentWithEmployeesAsDTO(Long id) {
        Department department = departmentRepository.findByIdWithEmployees(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        return convertToDepartmentDTOWithEmployees(department);
    }
    
    /**
     * Search departments by name as DTOs (without employees for performance)
     */
    @Transactional(readOnly = true)
    public List<DepartmentDTO> searchDepartmentsByNameAsDTO(String name) {
        List<Department> departments = departmentRepository.findByNameContainingIgnoreCase(name);
        return departments.stream()
                .map(this::convertToDepartmentDTO)
                .collect(Collectors.toList());
    }
    
    // Mapper methods
    
    /**
     * Convert Department entity to DTO without employees (for list views)
     */
    private DepartmentDTO convertToDepartmentDTO(Department department) {
        return new DepartmentDTO(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getCreatedAt(),
                department.getUpdatedAt(),
                department.getEmployees() != null ? department.getEmployees().size() : 0
        );
    }
    
    /**
     * Convert Department entity to DTO with employees (for detailed views)
     */
    private DepartmentDTO convertToDepartmentDTOWithEmployees(Department department) {
        DepartmentDTO dto = new DepartmentDTO(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getCreatedAt(),
                department.getUpdatedAt()
        );
        
        if (department.getEmployees() != null) {
            List<EmployeeDTO> employeeDTOs = department.getEmployees().stream()
                    .map(this::convertToEmployeeDTO)
                    .collect(Collectors.toList());
            dto.setEmployees(employeeDTOs);
        }

        return dto;
    }
    
    /**
     * Convert Employee entity to DTO (basic info for department responses)
     */
    private EmployeeDTO convertToEmployeeDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO(
                employee.getId(),
                employee.getName(),
                employee.getEmail(),
                employee.getPosition(),
                employee.getDateOfJoining(),
                employee.getSalary()
        );
        
        // Add department info if available (prevents circular reference)
        if (employee.getDepartment() != null) {
            EmployeeDTO.DepartmentBasicInfo deptInfo = new EmployeeDTO.DepartmentBasicInfo(
                    employee.getDepartment().getId(),
                    employee.getDepartment().getName(),
                    employee.getDepartment().getDescription()
            );
            dto.setDepartment(deptInfo);
        }
        
        // Add optional fields
        dto.withPhone(employee.getPhone())
           .withTimestamps(employee.getCreatedAt(), employee.getUpdatedAt());
        
        return dto;
    }
}