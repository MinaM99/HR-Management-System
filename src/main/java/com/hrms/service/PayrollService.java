package com.hrms.service;

import com.hrms.entity.Payroll;
import com.hrms.entity.Employee;
import com.hrms.exception.ResourceNotFoundException;
import com.hrms.exception.BadRequestException;
import com.hrms.exception.DuplicateResourceException;
import com.hrms.repository.PayrollRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PayrollService {
    
    private final PayrollRepository payrollRepository;
    private final EmployeeService employeeService;
    private final LeaveRequestService leaveRequestService;
    
    @Autowired
    public PayrollService(PayrollRepository payrollRepository, 
                         EmployeeService employeeService,
                         LeaveRequestService leaveRequestService) {
        this.payrollRepository = payrollRepository;
        this.employeeService = employeeService;
        this.leaveRequestService = leaveRequestService;
    }
    
    /**
     * Get all payroll records
     */
    @Transactional(readOnly = true)
    public List<Payroll> getAllPayrolls() {
        return payrollRepository.findRecentPayrolls();
    }
    
    /**
     * Get payroll by id
     */
    @Transactional(readOnly = true)
    public Payroll getPayrollById(Long id) {
        return payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll", "id", id));
    }
    
    /**
     * Create new payroll record
     */
    public Payroll createPayroll(Payroll payroll) {
        // Validate employee exists
        Employee employee = employeeService.getEmployeeById(payroll.getEmployee().getId());
        payroll.setEmployee(employee);
        
        // Validate month and year
        validateMonthAndYear(payroll.getMonth(), payroll.getYear());
        
        // Check if payroll already exists for this employee, month, and year
        if (payrollRepository.existsByEmployeeIdAndMonthAndYear(
                employee.getId(), payroll.getMonth(), payroll.getYear())) {
            throw new DuplicateResourceException(
                String.format("Payroll already exists for employee %s in %d/%d", 
                    employee.getName(), payroll.getMonth(), payroll.getYear()));
        }
        
        // Set employee's current salary if total salary is not provided
        if (payroll.getTotalSalary() == null) {
            payroll.setTotalSalary(employee.getSalary());
        }
        
        // Calculate leave days taken if not provided
        if (payroll.getLeaveDaysTaken() == null) {
            Long leaveDays = leaveRequestService.getApprovedLeaveDaysCount(
                employee.getId(), payroll.getYear(), payroll.getMonth());
            payroll.setLeaveDaysTaken(leaveDays.intValue());
        }
        
        // Set default values if not provided
        if (payroll.getDeductions() == null) {
            payroll.setDeductions(BigDecimal.ZERO);
        }
        if (payroll.getBonuses() == null) {
            payroll.setBonuses(BigDecimal.ZERO);
        }
        
        return payrollRepository.save(payroll);
    }
    
    /**
     * Update payroll record
     */
    public Payroll updatePayroll(Long id, Payroll payrollDetails) {
        Payroll payroll = getPayrollById(id);
        
        // Validate month and year if being updated
        if (payrollDetails.getMonth() != null || payrollDetails.getYear() != null) {
            Integer month = payrollDetails.getMonth() != null ? payrollDetails.getMonth() : payroll.getMonth();
            Integer year = payrollDetails.getYear() != null ? payrollDetails.getYear() : payroll.getYear();
            validateMonthAndYear(month, year);
            
            // Check if another payroll exists with the new month/year combination (excluding current)
            Optional<Payroll> existingPayroll = payrollRepository.findByEmployeeIdAndMonthAndYear(
                    payroll.getEmployee().getId(), month, year);
            if (existingPayroll.isPresent() && !existingPayroll.get().getId().equals(id)) {
                throw new DuplicateResourceException(
                    String.format("Payroll already exists for employee %s in %d/%d", 
                        payroll.getEmployee().getName(), month, year));
            }
            
            payroll.setMonth(month);
            payroll.setYear(year);
        }
        
        // Update other fields
        if (payrollDetails.getTotalSalary() != null) {
            validateSalary(payrollDetails.getTotalSalary());
            payroll.setTotalSalary(payrollDetails.getTotalSalary());
        }
        
        if (payrollDetails.getDeductions() != null) {
            validateAmount(payrollDetails.getDeductions(), "Deductions");
            payroll.setDeductions(payrollDetails.getDeductions());
        }
        
        if (payrollDetails.getBonuses() != null) {
            validateAmount(payrollDetails.getBonuses(), "Bonuses");
            payroll.setBonuses(payrollDetails.getBonuses());
        }
        
        if (payrollDetails.getWorkingDays() != null) {
            validateWorkingDays(payrollDetails.getWorkingDays());
            payroll.setWorkingDays(payrollDetails.getWorkingDays());
        }
        
        if (payrollDetails.getLeaveDaysTaken() != null) {
            validateLeaveDays(payrollDetails.getLeaveDaysTaken());
            payroll.setLeaveDaysTaken(payrollDetails.getLeaveDaysTaken());
        }
        
        return payrollRepository.save(payroll);
    }
    
    /**
     * Delete payroll record
     */
    public void deletePayroll(Long id) {
        Payroll payroll = getPayrollById(id);
        payrollRepository.delete(payroll);
    }
    
    /**
     * Generate payroll for employee
     */
    public Payroll generatePayrollForEmployee(Long employeeId, Integer month, Integer year) {
        Employee employee = employeeService.getEmployeeById(employeeId);
        
        // Check if payroll already exists
        if (payrollRepository.existsByEmployeeIdAndMonthAndYear(employeeId, month, year)) {
            throw new DuplicateResourceException(
                String.format("Payroll already exists for employee %s in %d/%d", 
                    employee.getName(), month, year));
        }
        
        // Create payroll record
        Payroll payroll = new Payroll(month, year, employee.getSalary(), employee);
        
        // Calculate leave days taken
        Long leaveDays = leaveRequestService.getApprovedLeaveDaysCount(employeeId, year, month);
        payroll.setLeaveDaysTaken(leaveDays.intValue());
        
        return payrollRepository.save(payroll);
    }
    
    /**
     * Get payroll records by employee
     */
    @Transactional(readOnly = true)
    public List<Payroll> getPayrollsByEmployee(Long employeeId) {
        return payrollRepository.findByEmployeeIdOrderByYearDescMonthDesc(employeeId);
    }
    
    /**
     * Get payroll by employee, month, and year
     */
    @Transactional(readOnly = true)
    public Payroll getPayrollByEmployeeAndPeriod(Long employeeId, Integer month, Integer year) {
        return payrollRepository.findByEmployeeIdAndMonthAndYear(employeeId, month, year)
                .orElseThrow(() -> new ResourceNotFoundException(
                    String.format("Payroll not found for employee %d in %d/%d", employeeId, month, year)));
    }
    
    /**
     * Get payroll records by month and year
     */
    @Transactional(readOnly = true)
    public List<Payroll> getPayrollsByPeriod(Integer month, Integer year) {
        return payrollRepository.findByMonthAndYear(month, year);
    }
    
    /**
     * Get payroll records by year
     */
    @Transactional(readOnly = true)
    public List<Payroll> getPayrollsByYear(Integer year) {
        return payrollRepository.findByYear(year);
    }
    
    /**
     * Get payroll records by employee and year
     */
    @Transactional(readOnly = true)
    public List<Payroll> getPayrollsByEmployeeAndYear(Long employeeId, Integer year) {
        return payrollRepository.findByEmployeeIdAndYear(employeeId, year);
    }
    
    /**
     * Get payroll records by department and period
     */
    @Transactional(readOnly = true)
    public List<Payroll> getPayrollsByDepartmentAndPeriod(Long departmentId, Integer month, Integer year) {
        return payrollRepository.findByDepartmentIdAndMonthAndYear(departmentId, month, year);
    }
    
    /**
     * Get total payroll cost by department
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTotalPayrollByDepartment(Integer month, Integer year) {
        return payrollRepository.getTotalPayrollByDepartment(month, year);
    }
    
    /**
     * Get total payroll cost for period
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPayrollCost(Integer month, Integer year) {
        return payrollRepository.getTotalPayrollCost(month, year);
    }
    
    /**
     * Get average salary by department
     */
    @Transactional(readOnly = true)
    public List<Object[]> getAverageSalaryByDepartment() {
        return payrollRepository.getAverageSalaryByDepartment();
    }
    
    /**
     * Get payroll statistics for a year
     */
    @Transactional(readOnly = true)
    public Object[] getPayrollStatisticsByYear(Integer year) {
        return payrollRepository.getPayrollStatisticsByYear(year);
    }
    
    /**
     * Get monthly payroll trends for a year
     */
    @Transactional(readOnly = true)
    public List<Object[]> getMonthlyPayrollTrends(Integer year) {
        return payrollRepository.getMonthlyPayrollTrends(year);
    }
    
    /**
     * Get payrolls by net pay range
     */
    @Transactional(readOnly = true)
    public List<Payroll> getPayrollsByNetPayRange(BigDecimal minAmount, BigDecimal maxAmount) {
        if (minAmount.compareTo(maxAmount) > 0) {
            throw new BadRequestException("Minimum amount cannot be greater than maximum amount");
        }
        return payrollRepository.findByNetPayBetween(minAmount, maxAmount);
    }
    
    // Validation methods
    
    private void validateMonthAndYear(Integer month, Integer year) {
        if (month == null || month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        
        if (year == null || year < 2000 || year > LocalDate.now().getYear() + 1) {
            throw new BadRequestException("Year must be between 2000 and " + (LocalDate.now().getYear() + 1));
        }
        
        // Don't allow future payroll beyond next month
        LocalDate today = LocalDate.now();
        LocalDate payrollDate = LocalDate.of(year, month, 1);
        if (payrollDate.isAfter(today.plusMonths(1))) {
            throw new BadRequestException("Cannot create payroll more than one month in advance");
        }
    }
    
    private void validateSalary(BigDecimal salary) {
        if (salary == null || salary.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Salary must be positive");
        }
    }
    
    private void validateAmount(BigDecimal amount, String fieldName) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException(fieldName + " cannot be negative");
        }
    }
    
    private void validateWorkingDays(Integer workingDays) {
        if (workingDays != null && (workingDays < 0 || workingDays > 31)) {
            throw new BadRequestException("Working days must be between 0 and 31");
        }
    }
    
    private void validateLeaveDays(Integer leaveDays) {
        if (leaveDays != null && (leaveDays < 0 || leaveDays > 31)) {
            throw new BadRequestException("Leave days must be between 0 and 31");
        }
    }
}