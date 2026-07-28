package com.convertexxx.validation;

import java.util.Locale;
import java.util.Set;

import com.convertexxx.exception.ValidationException;
import com.convertexxx.util.FileUtil;

public final class FileExtensionValidator {

    private FileExtensionValidator() {
    }

    public static void validate(String fileName, Set<String> allowedExtensions) {
        if (allowedExtensions == null || allowedExtensions.isEmpty()) {
            throw new IllegalArgumentException("At least one allowed file extension is required.");
        }
        String extension = FileUtil.extensionOf(fileName);
        boolean isAllowed = allowedExtensions.stream()
                .filter(extensionName -> extensionName != null)
                .map(extensionName -> extensionName.toLowerCase(Locale.ROOT))
                .map(extensionName -> extensionName.startsWith(".") ? extensionName.substring(1) : extensionName)
                .anyMatch(extension::equals);
        if (!isAllowed) {
            throw new ValidationException("File extension is not allowed.");
        }
    }
}
