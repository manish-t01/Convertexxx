package com.convertexxx.exception;

import com.convertexxx.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(InvalidFileException.class)
    ResponseEntity<ApiResponse<Void>> handleInvalidFileException(InvalidFileException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(FileStorageException.class)
    ResponseEntity<ApiResponse<Void>> handleFileStorageException(FileStorageException exception) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return response(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private ResponseEntity<ApiResponse<Void>> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(ApiResponse.failure(message));
    }
}
