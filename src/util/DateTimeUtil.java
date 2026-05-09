package util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateTimeUtil {
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateTimeUtil() {
    }

    public static LocalDateTime parseDateTime(String text) {
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.parse(text, DISPLAY_FORMATTER);
        }
    }

    public static Duration parseDurationHours(long hours) {
        if (hours <= 0) {
            throw new IllegalArgumentException("Duration hours must be positive");
        }
        return Duration.ofHours(hours);
    }

    public static Duration parseDurationMinutes(long minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Duration minutes must be positive");
        }
        return Duration.ofMinutes(minutes);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DISPLAY_FORMATTER);
    }
}
