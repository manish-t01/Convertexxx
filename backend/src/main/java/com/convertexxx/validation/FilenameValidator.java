package com.convertexxx.validation;

import com.convertexxx.exception.ValidationException;
import com.convertexxx.util.FileUtil;

public final class FilenameValidator {

    private FilenameValidator() {
    }

    public static void validate(String fileName) {
        if (!FileUtil.isSafeFileName(fileName)) {
            throw new ValidationException("Filename is invalid.");
        }
    }
}
