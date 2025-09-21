package com.hrms.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Standardized API Response wrapper for all endpoint responses.
 * 
 * This generic response wrapper ensures consistent API response structure
 * across all endpoints in the HR Management System.
 * 
 * @param <T> The type of data contained in the response
 */
@Schema(description = "Standard API response wrapper with success indicator, message, data payload, and timestamp")
public class ApiResponse<T> {
    
    @Schema(description = "Indicates if the operation was successful", example = "true")
    private boolean success;
    
    @Schema(description = "Human-readable message describing the operation result", 
            example = "Login successful - authentication cookies set")
    private String message;
    
    @Schema(description = "Response data payload (type varies by endpoint)")
    private T data;
    
    @Schema(description = "Response timestamp in ISO format", 
            example = "2025-09-21T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }
    
    public ApiResponse(boolean success, String message, T data) {
        this(success, message);
        this.data = data;
    }
    
    // Static factory methods for common responses
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", data);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
    
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}