package com.convertexxx.util;

public final class StringUtil {

    private StringUtil() {
    }

    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public static String truncate(String value, int maximumLength) {
        if (maximumLength < 0) {
            throw new IllegalArgumentException("Maximum length must not be negative.");
        }
        if (value == null || value.length() <= maximumLength) {
            return value;
        }
        return value.substring(0, maximumLength);
    }
}
