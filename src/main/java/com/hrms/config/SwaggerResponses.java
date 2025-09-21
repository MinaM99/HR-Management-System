package com.hrms.config;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

/**
 * Utility class containing pre-configured Swagger API response annotations.
 * 
 * This class provides reusable @ApiResponses annotations for common HTTP status codes
 * to reduce code duplication across controllers and ensure consistent documentation.
 * 
 * Usage:
 * Instead of writing repetitive @ApiResponses in each controller method,
 * use the pre-configured annotations from this class.
 * 
 * @author HR Management System Team
 * @version 1.0
 * @since 2025-09-21
 */
public final class SwaggerResponses {

    private SwaggerResponses() {
        // Utility class - prevent instantiation
    }

    /**
     * Standard success response (200 OK)
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operation successful")
    })
    public @interface Success {}

    /**
     * Authentication responses for login/refresh operations
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful - tokens stored in HTTP-only cookies"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or missing credentials"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials or authentication failed"),
        @ApiResponse(responseCode = "423", description = "Account locked due to multiple failed attempts"),
        @ApiResponse(responseCode = "403", description = "Account disabled - contact administrator"),
        @ApiResponse(responseCode = "500", description = "Authentication service temporarily unavailable")
    })
    public @interface AuthenticationResponses {}

    /**
     * Token refresh specific responses
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully - new access token stored in cookie"),
        @ApiResponse(responseCode = "400", description = "Refresh token cookie not found"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
        @ApiResponse(responseCode = "500", description = "Token refresh service temporarily unavailable")
    })
    public @interface TokenRefreshResponses {}

    /**
     * User registration responses
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid registration data or password confirmation mismatch"),
        @ApiResponse(responseCode = "409", description = "Username or email already exists"),
        @ApiResponse(responseCode = "500", description = "Registration service temporarily unavailable")
    })
    public @interface RegistrationResponses {}

    /**
     * Logout responses
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful - authentication cookies cleared")
    })
    public @interface LogoutResponses {}

    /**
     * Standard CRUD operation responses
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operation completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "404", description = "Resource not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public @interface CrudResponses {}

    /**
     * Data validation responses
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validation completed"),
        @ApiResponse(responseCode = "400", description = "Invalid data format"),
        @ApiResponse(responseCode = "409", description = "Data already exists or conflicts with existing data")
    })
    public @interface ValidationResponses {}

    /**
     * Employee management specific responses
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Employee operation successful"),
        @ApiResponse(responseCode = "201", description = "Employee created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid employee data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions for employee operations"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "409", description = "Employee data conflicts with existing records"),
        @ApiResponse(responseCode = "500", description = "Employee management service error")
    })
    public @interface EmployeeResponses {}

    /**
     * Department management specific responses
     */
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Department operation successful"),
        @ApiResponse(responseCode = "201", description = "Department created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid department data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions for department operations"),
        @ApiResponse(responseCode = "404", description = "Department not found"),
        @ApiResponse(responseCode = "409", description = "Department name already exists"),
        @ApiResponse(responseCode = "500", description = "Department management service error")
    })
    public @interface DepartmentResponses {}
}