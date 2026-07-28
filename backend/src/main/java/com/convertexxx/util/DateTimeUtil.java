package com.convertexxx.util;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class DateTimeUtil {

    private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneOffset.UTC);

    private DateTimeUtil() {
    }

    public static String formatUtc(Instant instant) {
        return UTC_FORMATTER.format(instant);
    }
}
