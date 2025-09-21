-- HR Management System Database Schema
-- This script creates the database schema and sample data
-- Note: JPA will automatically create tables with hibernate.ddl-auto=create-drop

-- Create database (if using MySQL directly)
-- CREATE DATABASE IF NOT EXISTS hr_management_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE hr_management_db;

-- Users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    failed_login_attempts INT NOT NULL DEFAULT 0
);

-- Roles table for authorization
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- User-Role mapping table (many-to-many)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Departments table
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Employees table (enhanced with user relationship)
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    position VARCHAR(100) NOT NULL,
    date_of_joining DATE NOT NULL,
    salary DECIMAL(10,2) NOT NULL,
    department_id BIGINT,
    user_id BIGINT UNIQUE NULL,  -- Link to user account (optional)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Leave requests table
CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    leave_type ENUM('SICK', 'VACATION', 'PERSONAL', 'MATERNITY', 'PATERNITY', 'EMERGENCY', 'OTHER') NOT NULL,
    reason VARCHAR(500),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    admin_comments VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Payroll table
CREATE TABLE IF NOT EXISTS payrolls (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    month INT NOT NULL CHECK (month >= 1 AND month <= 12),
    year INT NOT NULL CHECK (year >= 2000),
    total_salary DECIMAL(10,2) NOT NULL,
    deductions DECIMAL(10,2) DEFAULT 0.00,
    bonuses DECIMAL(10,2) DEFAULT 0.00,
    net_pay DECIMAL(10,2),
    working_days INT,
    leave_days_taken INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    UNIQUE KEY unique_employee_period (employee_id, month, year)
);

-- Indexes for better query performance
-- User table indexes
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_enabled ON users(enabled);
CREATE INDEX idx_user_last_login ON users(last_login);

-- Role table indexes
CREATE INDEX idx_role_name ON roles(name);

-- Employee table indexes
CREATE INDEX idx_employee_department ON employees(department_id);
CREATE INDEX idx_employee_position ON employees(position);
CREATE INDEX idx_employee_salary ON employees(salary);
CREATE INDEX idx_employee_joining_date ON employees(date_of_joining);
CREATE INDEX idx_employee_user ON employees(user_id);

-- Leave request indexes
CREATE INDEX idx_leave_request_employee ON leave_requests(employee_id);
CREATE INDEX idx_leave_request_status ON leave_requests(status);
CREATE INDEX idx_leave_request_dates ON leave_requests(start_date, end_date);

-- Payroll indexes
CREATE INDEX idx_payroll_employee ON payrolls(employee_id);
CREATE INDEX idx_payroll_period ON payrolls(year, month);

-- Insert default roles
INSERT INTO roles (name, description) VALUES 
('ADMIN', 'System administrator with full access'),
('HR', 'Human resources personnel with employee management access'),
('MANAGER', 'Department managers with team management access'),
('EMPLOYEE', 'Regular employees with basic self-service access');

-- Create default admin user
-- Password: Admin@123 (BCrypt encoded - correct hash)
INSERT INTO users (username, email, password, full_name, enabled) VALUES 
('admin', 'admin@company.com', '$2a$10$la4qX3T3plzoRglgGqnPuOhnnPSTedDeUuZN/JLaFxqDAgbmEdgxi', 'System Administrator', TRUE),
('hr.manager', 'hr@company.com', '$2a$10$la4qX3T3plzoRglgGqnPuOhnnPSTedDeUuZN/JLaFxqDAgbmEdgxi', 'HR Manager', TRUE),
('demo.user', 'demo@company.com', '$2a$10$la4qX3T3plzoRglgGqnPuOhnnPSTedDeUuZN/JLaFxqDAgbmEdgxi', 'Demo User', TRUE);

-- Assign roles to default users
INSERT INTO user_roles (user_id, role_id) VALUES 
(1, 1), -- admin -> ADMIN role
(1, 2), -- admin -> HR role (for testing)
(2, 2), -- hr.manager -> HR role
(3, 4); -- demo.user -> EMPLOYEE role

-- Sample data for testing
INSERT INTO departments (name, description) VALUES 
('Human Resources', 'Manages employee relations, policies, and benefits'),
('Information Technology', 'Develops and maintains technology infrastructure'),
('Finance', 'Handles financial planning, accounting, and budgeting'),
('Marketing', 'Promotes products and manages brand communications'),
('Operations', 'Oversees daily business operations and logistics'),
('Sales', 'Manages customer relationships and sales processes');

INSERT INTO employees (name, email, phone, position, date_of_joining, salary, department_id) VALUES 
('John Doe', 'john.doe@company.com', '+1234567890', 'Software Developer', '2023-01-15', 75000.00, 2),
('Jane Smith', 'jane.smith@company.com', '+1234567891', 'HR Manager', '2022-03-10', 85000.00, 1),
('Bob Johnson', 'bob.johnson@company.com', '+1234567892', 'Financial Analyst', '2023-06-20', 70000.00, 3),
('Alice Brown', 'alice.brown@company.com', '+1234567893', 'Marketing Specialist', '2023-02-28', 65000.00, 4),
('Charlie Wilson', 'charlie.wilson@company.com', '+1234567894', 'Operations Manager', '2022-11-05', 90000.00, 5),
('Diana Lee', 'diana.lee@company.com', '+1234567895', 'Sales Representative', '2023-04-12', 60000.00, 6),
('Edward Davis', 'edward.davis@company.com', '+1234567896', 'Senior Developer', '2021-09-01', 95000.00, 2),
('Fiona Garcia', 'fiona.garcia@company.com', '+1234567897', 'HR Specialist', '2023-07-15', 55000.00, 1);

INSERT INTO leave_requests (employee_id, start_date, end_date, leave_type, reason, status) VALUES 
(1, '2024-12-23', '2024-12-27', 'VACATION', 'Christmas holidays', 'APPROVED'),
(2, '2024-01-15', '2024-01-17', 'SICK', 'Flu symptoms', 'APPROVED'),
(3, '2024-02-10', '2024-02-12', 'PERSONAL', 'Family matters', 'PENDING'),
(4, '2024-03-05', '2024-03-07', 'VACATION', 'Short break', 'REJECTED'),
(5, '2024-04-20', '2024-04-22', 'SICK', 'Medical checkup', 'APPROVED'),
(6, '2024-05-15', '2024-05-20', 'VACATION', 'Summer vacation', 'PENDING');

INSERT INTO payrolls (employee_id, month, year, total_salary, deductions, bonuses, net_pay, working_days, leave_days_taken) VALUES 
(1, 11, 2024, 75000.00, 7500.00, 5000.00, 72500.00, 22, 0),
(2, 11, 2024, 85000.00, 8500.00, 3000.00, 79500.00, 22, 0),
(3, 11, 2024, 70000.00, 7000.00, 2000.00, 65000.00, 22, 0),
(4, 11, 2024, 65000.00, 6500.00, 1500.00, 60000.00, 22, 0),
(5, 11, 2024, 90000.00, 9000.00, 4000.00, 85000.00, 22, 0),
(6, 11, 2024, 60000.00, 6000.00, 1000.00, 55000.00, 22, 0);

COMMIT;