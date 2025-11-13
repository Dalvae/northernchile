package com.northernchile.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.InstantDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson Configuration for consistent date/time serialization between backend and frontend.
 *
 * Date/Time Serialization Contracts:
 * ==================================
 *
 * 1. Instant (e.g., TourSchedule.startDatetime, Booking.createdAt)
 *    - Serialized as: ISO-8601 UTC string with timezone offset
 *    - Format: "2025-01-15T10:30:00Z" or "2025-01-15T10:30:00.123Z"
 *    - Use case: Timestamps that need timezone awareness
 *    - Frontend handling: Can parse directly with new Date() or date libraries
 *
 * 2. LocalDate (e.g., User.dateOfBirth, Participant.dateOfBirth)
 *    - Serialized as: ISO-8601 date-only string
 *    - Format: "2025-01-15" (always YYYY-MM-DD)
 *    - Use case: Dates without time or timezone context (birthdays, etc.)
 *    - Frontend handling: HTML date inputs expect this format directly
 *
 * 3. LocalTime (e.g., Tour.defaultStartTime)
 *    - Serialized as: ISO-8601 time-only string
 *    - Format: "10:30:00" or "10:30:00.123" (HH:mm:ss with optional milliseconds)
 *    - Use case: Time of day without date context
 *    - Frontend handling: HTML time inputs expect HH:mm format (slice first 5 chars)
 *
 * Database Storage:
 * =================
 * - All Instant fields stored as UTC in database (configured via hibernate.jdbc.time_zone=UTC)
 * - LocalDate stored as DATE type (no timezone)
 * - LocalTime stored as TIME type (no timezone)
 *
 * Important Notes:
 * ================
 * - Frontend should NOT convert LocalDate to Date objects (lose timezone context)
 * - Instant fields automatically handle timezone conversions
 * - When sending dates from frontend to backend:
 *   - LocalDate: Send as "YYYY-MM-DD" string
 *   - LocalTime: Send as "HH:mm:ss" string
 *   - Instant: Send as ISO-8601 UTC string or let backend parse from LocalDate + LocalTime
 */
@Configuration
public class JacksonConfig {

    /**
     * Standard ISO-8601 date format: YYYY-MM-DD
     * Example: 2025-01-15
     */
    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Standard ISO-8601 time format: HH:mm:ss
     * Example: 14:30:00
     */
    private static final DateTimeFormatter LOCAL_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Create custom JavaTimeModule with explicit formatters
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // Configure LocalDate serialization/deserialization (YYYY-MM-DD)
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(LOCAL_DATE_FORMATTER));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(LOCAL_DATE_FORMATTER));

        // Configure LocalTime serialization/deserialization (HH:mm:ss)
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(LOCAL_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(LOCAL_TIME_FORMATTER));

        // Configure Instant serialization (ISO-8601 UTC with 'Z' suffix)
        javaTimeModule.addSerializer(Instant.class, InstantSerializer.INSTANCE);
        javaTimeModule.addDeserializer(Instant.class, InstantDeserializer.INSTANT);

        // Register the module
        mapper.registerModule(javaTimeModule);

        // Disable writing dates as timestamps (use ISO-8601 strings instead)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Write BigDecimal as plain strings (not scientific notation)
        mapper.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);

        return mapper;
    }
}
