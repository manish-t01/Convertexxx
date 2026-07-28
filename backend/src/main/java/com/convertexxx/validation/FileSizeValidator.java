package com.convertexxx.validation;

import com.convertexxx.exception.ValidationException;

public final class FileSizeValidator {

    private FileSizeValidator() {
    }

    public static void validate(long fileSize, long maximumFileSize) {
        if (fileSize < 0) {
            throw new ValidationException("File size must not be negative.");
        }
        if (maximumFileSize < 0) {
            throw new IllegalArgumentException("Maximum file size must not be negative.");
        }
        if (fileSize > maximumFileSize) {
            throw new ValidationException("File size exceeds the allowed limit.");
        }
    }
}
