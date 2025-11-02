package com.northernchile.api.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date/time operations in Chile timezone.
 *
 * IMPORTANT: All tour schedules should be stored as Instant (UTC) in the database,
 * but created and displayed using Chile timezone (America/Santiago).
 *
 * Chile observes DST:
 * - Winter: UTC-04:00 (typically April to September)
 * - Summer: UTC-03:00 (typically September to April)
 *
 * Java automatically handles these transitions.
 */
public class DateTimeUtils {

    private static final ZoneId CHILE_ZONE = ZoneId.of("America/Santiago");

    /**
     * Get the current date/time in Chile timezone
     */
    public static ZonedDateTime nowInChile() {
        return ZonedDateTime.now(CHILE_ZONE);
    }

    /**
     * Get the current date in Chile timezone
     */
    public static LocalDate todayInChile() {
        return LocalDate.now(CHILE_ZONE);
    }

    /**
     * Convert a LocalDate to Instant at start of day in Chile timezone
     */
    public static Instant toInstantStartOfDay(LocalDate date) {
        return date.atStartOfDay(CHILE_ZONE).toInstant();
    }

    /**
     * Convert a LocalDate to Instant at end of day in Chile timezone
     */
    public static Instant toInstantEndOfDay(LocalDate date) {
        return date.atTime(23, 59, 59).atZone(CHILE_ZONE).toInstant();
    }

    /**
     * Convert LocalDate and LocalTime to Instant in Chile timezone
     */
    public static Instant toInstant(LocalDate date, LocalTime time) {
        return ZonedDateTime.of(date, time, CHILE_ZONE).toInstant();
    }

    /**
     * Convert LocalDateTime to Instant in Chile timezone
     */
    public static Instant toInstant(LocalDateTime dateTime) {
        return dateTime.atZone(CHILE_ZONE).toInstant();
    }

    /**
     * Convert Instant to ZonedDateTime in Chile timezone
     */
    public static ZonedDateTime toChileTime(Instant instant) {
        return instant.atZone(CHILE_ZONE);
    }

    /**
     * Convert Instant to LocalDate in Chile timezone
     */
    public static LocalDate toLocalDate(Instant instant) {
        return instant.atZone(CHILE_ZONE).toLocalDate();
    }

    /**
     * Convert Instant to LocalTime in Chile timezone
     */
    public static LocalTime toLocalTime(Instant instant) {
        return instant.atZone(CHILE_ZONE).toLocalTime();
    }

    /**
     * Format an Instant for display in Chile timezone
     */
    public static String formatForDisplay(Instant instant, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(CHILE_ZONE);
        return formatter.format(instant);
    }

    /**
     * Check if a given date is in DST (summer time) in Chile
     */
    public static boolean isInDST(LocalDate date) {
        ZonedDateTime zdt = date.atStartOfDay(CHILE_ZONE);
        return CHILE_ZONE.getRules().isDaylightSavings(zdt.toInstant());
    }

    /**
     * Get the current UTC offset for Chile (accounting for DST)
     */
    public static ZoneOffset getCurrentChileOffset() {
        return CHILE_ZONE.getRules().getOffset(Instant.now());
    }

    /**
     * Get UTC offset for a specific instant
     */
    public static ZoneOffset getChileOffset(Instant instant) {
        return CHILE_ZONE.getRules().getOffset(instant);
    }
}
