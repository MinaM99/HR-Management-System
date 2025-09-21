package com.hrms.service;

import com.hrms.entity.Employee;
import com.hrms.entity.Department;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.exception.BadRequestException;
import com.hrms.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final DepartmentService departmentService;
    
    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository, DepartmentService departmentService) {
        this.employeeRepository = employeeRepository;
        this.departmentService = departmentService;
    }
    
    /**
     * Get all employees
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAllByOrderByNameAsc();
    }
    
    /**
     * Get employee by id
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }
    
    /**
     * Get employee by email
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "email", email));
    }
    
    /**
     * Create new employee
     */
    public Employee createEmployee(Employee employee) {
        // Check if employee with same email already exists
        if (employeeRepository.existsByEmail(employee.getEmail())) {
            throw new DuplicateResourceException("Employee", "email", employee.getEmail());
        }
        
        // Validate date of joining is not in the future
        if (employee.getDateOfJoining().isAfter(LocalDate.now())) {
            throw new BadRequestException("Date of joining cannot be in the future");
        }
        
        // Validate salary is positive
        if (employee.getSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Salary must be positive");
        }
        
        // Validate department exists if provided
        if (employee.getDepartment() != null && employee.getDepartment().getId() != null) {
            Department department = departmentService.getDepartmentById(employee.getDepartment().getId());
            employee.setDepartment(department);
        }
        
        return employeeRepository.save(employee);
    }
    
    /**
     * Update existing employee
     */
    public Employee updateEmployee(Long id, Employee employeeDetails) {
        Employee employee = getEmployeeById(id);
        
        // Check if another employee with the same email exists (excluding current one)
        employeeRepository.findByEmail(employeeDetails.getEmail())
                .ifPresent(existingEmp -> {
                    if (!existingEmp.getId().equals(id)) {
                        throw new DuplicateResourceException("Employee", "email", employeeDetails.getEmail());
                    }
                });
        
        // Validate date of joining is not in the future
        if (employeeDetails.getDateOfJoining().isAfter(LocalDate.now())) {
            throw new BadRequestException("Date of joining cannot be in the future");
        }
        
        // Validate salary is positive
        if (employeeDetails.getSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Salary must be positive");
        }
        
        // Validate department exists if provided
        if (employeeDetails.getDepartment() != null && employeeDetails.getDepartment().getId() != null) {
            Department department = departmentService.getDepartmentById(employeeDetails.getDepartment().getId());
            employee.setDepartment(department);
        }
        
        // Update employee details
        employee.setName(employeeDetails.getName());
        employee.setEmail(employeeDetails.getEmail());
        employee.setPhone(employeeDetails.getPhone());
        employee.setPosition(employeeDetails.getPosition());
        employee.setDateOfJoining(employeeDetails.getDateOfJoining());
        employee.setSalary(employeeDetails.getSalary());
        
        return employeeRepository.save(employee);
    }
    
    /**
     * Delete employee
     */
    public void deleteEmployee(Long id) {
        Employee employee = getEmployeeById(id);
        employeeRepository.delete(employee);
    }
    
    /**
     * Get employees by department
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId);
    }
    
    /**
     * Get employees by department name
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDepartmentName(String departmentName) {
        return employeeRepository.findByDepartmentName(departmentName);
    }
    
    /**
     * Get employees by position
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByPosition(String position) {
        return employeeRepository.findByPosition(position);
    }
    
    /**
     * Search employees by name
     */
    @Transactional(readOnly = true)
    public List<Employee> searchEmployeesByName(String name) {
        return employeeRepository.findByNameContainingIgnoreCase(name);
    }
    
    /**
     * Search employees by position
     */
    @Transactional(readOnly = true)
    public List<Employee> searchEmployeesByPosition(String position) {
        return employeeRepository.findByPositionContainingIgnoreCase(position);
    }
    
    /**
     * Get employees by salary range
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesBySalaryRange(BigDecimal minSalary, BigDecimal maxSalary) {
        if (minSalary.compareTo(maxSalary) > 0) {
            throw new BadRequestException("Minimum salary cannot be greater than maximum salary");
        }
        return employeeRepository.findBySalaryBetween(minSalary, maxSalary);
    }
    
    /**
     * Get employees who joined after a date
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesJoinedAfter(LocalDate date) {
        return employeeRepository.findByDateOfJoiningAfter(date);
    }
    
    /**
     * Get employees who joined before a date
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesJoinedBefore(LocalDate date) {
        return employeeRepository.findByDateOfJoiningBefore(date);
    }
    
    /**
     * Get employees who joined between dates
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesJoinedBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        return employeeRepository.findByDateOfJoiningBetween(startDate, endDate);
    }
    
    /**
     * Get employees ordered by salary (highest first)
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesOrderedBySalary() {
        return employeeRepository.findAllByOrderBySalaryDesc();
    }
    
    /**
     * Get employees ordered by joining date (newest first)
     */
    @Transactional(readOnly = true)
    public List<Employee> getEmployeesOrderedByJoiningDate() {
        return employeeRepository.findAllByOrderByDateOfJoiningDesc();
    }
    
    /**
     * Get employee with department details
     */
    @Transactional(readOnly = true)
    public Employee getEmployeeWithDepartment(Long id) {
        return employeeRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }
    
    /**
     * Search employees by multiple criteria
     */
    @Transactional(readOnly = true)
    public List<Employee> searchEmployees(String name, String position, String departmentName) {
        return employeeRepository.searchEmployees(name, position, departmentName);
    }
    
    /**
     * Get employee count by department
     */
    @Transactional(readOnly = true)
    public List<Object[]> getEmployeeCountByDepartment() {
        return employeeRepository.getEmployeeCountByDepartment();
    }
    
    /**
     * Check if employee exists
     */
    @Transactional(readOnly = true)
    public boolean employeeExists(Long id) {
        return employeeRepository.existsById(id);
    }
    
    /**
     * Check if email is available
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !employeeRepository.existsByEmail(email);
    }
}