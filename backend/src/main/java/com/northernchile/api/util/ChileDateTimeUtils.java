package com.northernchile.api.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Utility class for handling date/time conversions with Chile timezone.
 * 
 * All tours operate in Chile timezone (America/Santiago).
 * This class provides centralized methods to convert between:
 * - LocalDate + LocalTime (user input) -> Instant (database storage)
 * - Instant (database) -> LocalDate/LocalTime (display)
 */
public final class ChileDateTimeUtils {
    
    public static final ZoneId CHILE_ZONE = ZoneId.of("America/Santiago");
    
    private ChileDateTimeUtils() {
        // Utility class, no instantiation
    }
    
    /**
     * Convert LocalDate + LocalTime to Instant using Chile timezone.
     * Use this when receiving date/time from frontend and storing in database.
     * 
     * @param date The local date in Chile
     * @param time The local time in Chile
     * @return Instant representing the exact moment in UTC
     */
    public static Instant toInstant(LocalDate date, LocalTime time) {
        if (date == null || time == null) {
            return null;
        }
        return ZonedDateTime.of(date, time, CHILE_ZONE).toInstant();
    }
    
    /**
     * Convert Instant to LocalDate in Chile timezone.
     * Use this when displaying dates from database to users.
     * 
     * @param instant The instant from database
     * @return LocalDate in Chile timezone
     */
    public static LocalDate toChileDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(CHILE_ZONE).toLocalDate();
    }
    
    /**
     * Convert Instant to LocalTime in Chile timezone.
     * Use this when displaying times from database to users.
     * 
     * @param instant The instant from database
     * @return LocalTime in Chile timezone
     */
    public static LocalTime toChileTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(CHILE_ZONE).toLocalTime();
    }
    
    /**
     * Get current date in Chile timezone.
     */
    public static LocalDate todayInChile() {
        return LocalDate.now(CHILE_ZONE);
    }
    
    /**
     * Get current time in Chile timezone.
     */
    public static LocalTime nowInChile() {
        return LocalTime.now(CHILE_ZONE);
    }
}
