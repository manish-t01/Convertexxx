package com.convertexxx.dto;

import java.time.Instant;

public record ApiResponse<T>(boolean success, String message, Instant timestamp, T data) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, Instant.now(), data);
    }

    public static ApiResponse<Void> failure(String message) {
        return new ApiResponse<>(false, message, Instant.now(), null);
    }
}
