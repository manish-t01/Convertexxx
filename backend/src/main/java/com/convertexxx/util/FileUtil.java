package com.convertexxx.util;

import java.util.Locale;

public final class FileUtil {

    private FileUtil() {
    }

    public static String extensionOf(String fileName) {
        if (!StringUtil.hasText(fileName)) {
            return "";
        }
        int extensionSeparator = fileName.lastIndexOf('.');
        if (extensionSeparator <= 0 || extensionSeparator == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(extensionSeparator + 1).toLowerCase(Locale.ROOT);
    }

    public static boolean isSafeFileName(String fileName) {
        return StringUtil.hasText(fileName)
                && !fileName.contains("/")
                && !fileName.contains("\\")
                && !fileName.contains("..")
                && fileName.chars().noneMatch(Character::isISOControl);
    }
}
