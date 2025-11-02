package com.northernchile.api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Configuration class to set the application's default timezone.
 *
 * Chile uses America/Santiago timezone which automatically handles DST:
 * - Winter (April-September): UTC-04:00 (CLT - Chile Standard Time)
 * - Summer (September-April): UTC-03:00 (CLST - Chile Summer Time)
 *
 * Java's ZoneId handles these transitions automatically based on Chilean law.
 */
@Configuration
public class TimezoneConfig {

    @Value("${app.timezone:America/Santiago}")
    private String timezone;

    public static final ZoneId CHILE_ZONE = ZoneId.of("America/Santiago");

    @PostConstruct
    public void init() {
        // Set default timezone for the entire JVM
        TimeZone.setDefault(TimeZone.getTimeZone(timezone));
        System.out.println("===============================================");
        System.out.println("Application timezone set to: " + timezone);
        System.out.println("Current offset: " + ZoneId.of(timezone).getRules().getOffset(java.time.Instant.now()));
        System.out.println("===============================================");
    }

    /**
     * Get the Chile timezone ZoneId for use in date/time operations
     */
    public static ZoneId getChileZone() {
        return CHILE_ZONE;
    }
}
