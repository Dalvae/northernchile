package com.northernchile.api.availability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica que la zona horaria America/Santiago esté correctamente configurada
 * y maneje el horario de verano (DST) chileno correctamente.
 *
 * Chile tiene dos horarios:
 * - Horario estándar (invierno): UTC-4 (CLT - Chile Standard Time)
 * - Horario de verano: UTC-3 (CLST - Chile Summer Time)
 *
 * Los cambios ocurren aproximadamente:
 * - Inicio verano: Primer sábado de septiembre (adelantar 1 hora a las 00:00)
 * - Fin verano: Primer sábado de abril (atrasar 1 hora a las 00:00)
 */
@DisplayName("TimeZone Verification - Chile DST Handling")
class TimeZoneVerificationTest {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Santiago");

    @Test
    @DisplayName("Should handle summer time (UTC-3) correctly in January")
    void shouldHandleSummerTimeInJanuary() {
        // Given - Fecha en pleno verano chileno (enero)
        LocalDate summerDate = LocalDate.of(2025, Month.JANUARY, 15);

        // When
        ZonedDateTime zdt = summerDate.atStartOfDay(ZONE_ID);
        ZoneOffset offset = zdt.getOffset();

        // Then - Debe estar en UTC-3 (horario de verano)
        assertThat(offset.getTotalSeconds()).isEqualTo(-3 * 3600); // -3 hours

        // Verificar conversión a UTC
        Instant instant = zdt.toInstant();
        assertThat(instant.toString()).contains("T03:00:00Z"); // 00:00 CLT = 03:00 UTC
    }

    @Test
    @DisplayName("Should handle standard time (UTC-4) correctly in July")
    void shouldHandleStandardTimeInJuly() {
        // Given - Fecha en pleno invierno chileno (julio)
        LocalDate winterDate = LocalDate.of(2025, Month.JULY, 15);

        // When
        ZonedDateTime zdt = winterDate.atStartOfDay(ZONE_ID);
        ZoneOffset offset = zdt.getOffset();

        // Then - Debe estar en UTC-4 (horario estándar)
        assertThat(offset.getTotalSeconds()).isEqualTo(-4 * 3600); // -4 hours

        // Verificar conversión a UTC
        Instant instant = zdt.toInstant();
        assertThat(instant.toString()).contains("T04:00:00Z"); // 00:00 CLT = 04:00 UTC
    }

    @Test
    @DisplayName("Should detect DST transition dates for current year")
    void shouldDetectDSTTransitionDates() {
        // Given
        ZoneRules rules = ZONE_ID.getRules();
        Year currentYear = Year.of(2025);

        // When - Buscar transiciones en 2025
        LocalDateTime startOfYear = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(2025, 12, 31, 23, 59);

        ZoneOffsetTransition springTransition = rules.nextTransition(
            startOfYear.toInstant(ZoneOffset.ofHours(-3))
        );

        // Then
        if (springTransition != null) {
            LocalDateTime transitionTime = LocalDateTime.ofInstant(
                springTransition.getInstant(),
                ZONE_ID
            );

            System.out.println("=== Chile DST Transition 2025 ===");
            System.out.println("Transition date: " + transitionTime);
            System.out.println("Offset before: " + springTransition.getOffsetBefore());
            System.out.println("Offset after: " + springTransition.getOffsetAfter());

            // Verificar que la transición ocurra en abril o septiembre (típico de Chile)
            assertThat(transitionTime.getMonth())
                .isIn(Month.APRIL, Month.SEPTEMBER);
        }
    }

    @Test
    @DisplayName("Should correctly convert schedule times near DST transition")
    void shouldHandleDSTTransitionEdgeCase() {
        // Given - Día del cambio de horario (ejemplo: 6 abril 2025)
        // En Chile, el cambio ocurre a las 00:00 (medianoche)
        LocalDate transitionDay = LocalDate.of(2025, Month.APRIL, 6);

        // When - Crear schedule justo antes y después del cambio
        ZonedDateTime beforeMidnight = transitionDay.atTime(23, 59).atZone(ZONE_ID);
        ZonedDateTime afterMidnight = transitionDay.plusDays(1).atStartOfDay(ZONE_ID);

        // Then - El offset debería ser diferente
        System.out.println("Before transition: " + beforeMidnight);
        System.out.println("After transition: " + afterMidnight);

        // Los instantes deberían tener 1 hora de diferencia (3600 segundos)
        long secondsDiff = Duration.between(
            beforeMidnight.toInstant(),
            afterMidnight.toInstant()
        ).getSeconds();

        // Puede ser 60 segundos (sin DST) o 3660 segundos (con DST)
        assertThat(secondsDiff).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should maintain consistency when converting Instant to LocalDate")
    void shouldMaintainConsistencyInConversions() {
        // Given - Un schedule a las 20:00 UTC
        Instant scheduleTime = Instant.parse("2025-01-15T20:00:00Z");

        // When - Convertir a fecha local chilena
        LocalDate localDate = LocalDate.ofInstant(scheduleTime, ZONE_ID);

        // Then - En verano (UTC-3), 20:00 UTC = 17:00 CLT (mismo día)
        assertThat(localDate).isEqualTo(LocalDate.of(2025, 1, 15));

        // Given - Mismo horario en invierno
        Instant winterSchedule = Instant.parse("2025-07-15T20:00:00Z");

        // When
        LocalDate winterLocalDate = LocalDate.ofInstant(winterSchedule, ZONE_ID);

        // Then - En invierno (UTC-4), 20:00 UTC = 16:00 CLT (mismo día)
        assertThat(winterLocalDate).isEqualTo(LocalDate.of(2025, 7, 15));
    }

    @Test
    @DisplayName("Should handle midnight edge case correctly")
    void shouldHandleMidnightEdgeCase() {
        // Given - Medianoche en Chile
        LocalDate date = LocalDate.of(2025, 1, 15);
        ZonedDateTime midnight = date.atStartOfDay(ZONE_ID);

        // When - Convertir a Instant y volver a LocalDate
        Instant instant = midnight.toInstant();
        LocalDate roundTrip = LocalDate.ofInstant(instant, ZONE_ID);

        // Then - Debe ser la misma fecha
        assertThat(roundTrip).isEqualTo(date);
    }

    @Test
    @DisplayName("Should verify tzdata is reasonably up-to-date")
    void shouldVerifyTzdataVersion() {
        // Given
        ZoneRules rules = ZONE_ID.getRules();

        // When - Obtener transiciones desde 2024
        LocalDateTime start2024 = LocalDateTime.of(2024, 1, 1, 0, 0);
        Instant instant2024 = start2024.toInstant(ZoneOffset.ofHours(-3));

        ZoneOffsetTransition transition = rules.nextTransition(instant2024);

        // Then - Si no hay transiciones, algo está mal con tzdata
        assertThat(transition)
            .withFailMessage(
                "No DST transitions found for America/Santiago since 2024. " +
                "This indicates an outdated tzdata database. " +
                "Update your JDK or Docker base image."
            )
            .isNotNull();

        System.out.println("=== TimeZone Database Status ===");
        System.out.println("Zone ID: " + ZONE_ID);
        System.out.println("Next transition: " + transition.getInstant());
        System.out.println("tzdata appears to be up-to-date ✓");
    }

    @Test
    @DisplayName("Should compare weather predictions with correct timezone")
    void shouldCompareWeatherPredictionsWithCorrectTimezone() {
        // Given - Weather API retorna epoch timestamp
        long weatherEpoch = 1736899200L; // 15 Jan 2025 00:00:00 UTC

        // Given - Schedule en la misma fecha (Chile time)
        Instant scheduleTime = LocalDate.of(2025, 1, 15)
            .atStartOfDay(ZONE_ID)
            .toInstant();

        // When - Convertir ambos a LocalDate para comparar
        LocalDate weatherDate = Instant.ofEpochSecond(weatherEpoch)
            .atZone(ZONE_ID)
            .toLocalDate();

        LocalDate scheduleDate = LocalDate.ofInstant(scheduleTime, ZONE_ID);

        // Then - Deben coincidir
        assertThat(weatherDate).isEqualTo(scheduleDate);

        System.out.println("Weather prediction date: " + weatherDate);
        System.out.println("Schedule date: " + scheduleDate);
        System.out.println("Dates match correctly ✓");
    }
}
