package com.northernchile.api.tour;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.northernchile.api.external.LunarService;
import com.northernchile.api.external.WeatherService;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.tour.TourRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static com.cronutils.model.CronType.UNIX;

@Service
public class TourScheduleGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(TourScheduleGeneratorService.class);

    // Límites de generación según tipo de tour
    private static final int DAYS_AHEAD_ASTRONOMICAL = 60; // 2 meses (solo luna - cálculo local)
    private static final int DAYS_AHEAD_WEATHER_SENSITIVE = 8; // Límite OpenWeatherMap
    private static final int DAYS_AHEAD_DEFAULT = 30; // 1 mes

    private static final double WIND_THRESHOLD_KNOTS = 25.0;
    private static final ZoneId ZONE_ID = ZoneId.of("America/Santiago");

    private final TourRepository tourRepository;
    private final TourScheduleRepository tourScheduleRepository;
    private final LunarService lunarService;
    private final WeatherService weatherService;
    private final CronParser cronParser;

    public TourScheduleGeneratorService(
            TourRepository tourRepository,
            TourScheduleRepository tourScheduleRepository,
            LunarService lunarService,
            WeatherService weatherService) {
        this.tourRepository = tourRepository;
        this.tourScheduleRepository = tourScheduleRepository;
        this.lunarService = lunarService;
        this.weatherService = weatherService;
        this.cronParser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(UNIX));
    }

    /**
     * Se ejecuta automáticamente cada día a las 2 AM
     * Genera schedules con límites diferenciados según tipo de tour:
     * - Tours astronómicos: 60 días (solo validación lunar - cálculo local)
     * - Tours sensibles al clima: 8 días (límite OpenWeatherMap)
     * - Tours normales: 30 días
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "America/Santiago")
    @Transactional
    public void generateSchedulesAutomatically() {
        logger.info("Iniciando generación automática de schedules");

        LocalDate today = LocalDate.now(ZONE_ID);

        // Solo tours recurrentes y publicados
        List<Tour> recurringTours = tourRepository.findByRecurringTrueAndStatus("PUBLISHED");

        logger.info("Encontrados {} tours recurrentes publicados", recurringTours.size());

        int schedulesCreated = 0;
        int schedulesSkipped = 0;

        for (Tour tour : recurringTours) {
            // Determinar límite de días según tipo de tour
            int daysToGenerate = getDaysToGenerate(tour);
            LocalDate endDate = today.plusDays(daysToGenerate);

            logger.debug("Tour {}: Generando schedules para {} días", tour.getId(), daysToGenerate);

            for (LocalDate date = today; date.isBefore(endDate); date = date.plusDays(1)) {
                try {
                    if (shouldGenerateScheduleForDate(tour, date)) {
                        if (!scheduleExists(tour, date)) {
                            createScheduleInstance(tour, date);
                            schedulesCreated++;
                        } else {
                            schedulesSkipped++;
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error generando schedule para tour {} en fecha {}: {}",
                            tour.getId(), date, e.getMessage());
                }
            }
        }

        logger.info("Generación completada. Creados: {}, Omitidos (ya existentes): {}",
                schedulesCreated, schedulesSkipped);
    }

    /**
     * Determina cuántos días adelante generar schedules según el tipo de tour
     */
    private int getDaysToGenerate(Tour tour) {
        // Tours astronómicos puros: 60 días (solo dependen de la luna - cálculo local sin límite)
        if (tour.isMoonSensitive() && !tour.isWindSensitive() && !tour.isCloudSensitive()) {
            return DAYS_AHEAD_ASTRONOMICAL;
        }

        // Tours sensibles al clima: 8 días (límite del pronóstico de OpenWeatherMap)
        if (tour.isWindSensitive() || tour.isCloudSensitive()) {
            return DAYS_AHEAD_WEATHER_SENSITIVE;
        }

        // Tours normales: 30 días por defecto
        return DAYS_AHEAD_DEFAULT;
    }

    /**
     * Determina si se debe generar un schedule para un tour en una fecha específica
     */
    private boolean shouldGenerateScheduleForDate(Tour tour, LocalDate date) {
        // 1. Verificar si coincide con la regla de recurrencia (cron)
        if (!matchesCronRule(date, tour.getRecurrenceRule())) {
            return false;
        }

        // 2. Aplicar restricciones de negocio
        return isDateValid(tour, date);
    }

    /**
     * Verifica si una fecha coincide con una regla cron
     */
    private boolean matchesCronRule(LocalDate date, String recurrenceRule) {
        if (recurrenceRule == null || recurrenceRule.isEmpty()) {
            // Sin regla, asumir diario
            return true;
        }

        try {
            Cron cron = cronParser.parse(recurrenceRule);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);

            // Verificar si la fecha coincide con la expresión cron
            ZonedDateTime dateTime = date.atStartOfDay(ZONE_ID);
            Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(dateTime.minusDays(1));

            return nextExecution.isPresent() && nextExecution.get().toLocalDate().equals(date);
        } catch (Exception e) {
            logger.warn("Error parseando cron rule '{}': {}", recurrenceRule, e.getMessage());
            // Si falla el parsing, asumir que aplica diario
            return true;
        }
    }

    /**
     * Valida si una fecha cumple con las restricciones de negocio del tour
     *
     * IMPORTANTE: Validaciones de clima solo se aplican dentro de 8 días
     * (límite del pronóstico de OpenWeatherMap). Más allá de 8 días, solo se valida luna.
     */
    private boolean isDateValid(Tour tour, LocalDate date) {
        LocalDate today = LocalDate.now(ZONE_ID);
        long daysFromNow = java.time.temporal.ChronoUnit.DAYS.between(today, date);

        // Restricción de luna llena para tours sensibles a la luna
        // (Sin límite de días - cálculo local siempre disponible)
        if (tour.isMoonSensitive() && lunarService.isFullMoon(date)) {
            logger.debug("Fecha {} omitida para tour {} por luna llena", date, tour.getId());
            return false;
        }

        // Restricciones de clima: Solo dentro de 8 días (límite de OpenWeatherMap)
        if (daysFromNow < DAYS_AHEAD_WEATHER_SENSITIVE) {
            // Restricción de viento para tours sensibles al viento
            if (tour.isWindSensitive() && weatherService.isWindAboveThreshold(date, WIND_THRESHOLD_KNOTS)) {
                logger.debug("Fecha {} omitida para tour {} por viento excesivo", date, tour.getId());
                return false;
            }

            // Restricción de nubosidad para tours sensibles a las nubes
            if (tour.isCloudSensitive() && weatherService.isCloudyDay(date)) {
                logger.debug("Fecha {} omitida para tour {} por nubosidad excesiva", date, tour.getId());
                return false;
            }
        }
        // Nota: Más allá de 8 días, solo validamos luna (no hay pronóstico confiable)

        return true;
    }

    /**
     * Verifica si ya existe un schedule para este tour en esta fecha
     */
    private boolean scheduleExists(Tour tour, LocalDate date) {
        ZonedDateTime startOfDay = date.atStartOfDay(ZONE_ID);
        ZonedDateTime endOfDay = date.plusDays(1).atStartOfDay(ZONE_ID);

        Instant startInstant = startOfDay.toInstant();
        Instant endInstant = endOfDay.toInstant();

        return !tourScheduleRepository.findByTourIdAndStartDatetimeBetween(
                tour.getId(),
                startInstant,
                endInstant
        ).isEmpty();
    }

    /**
     * Crea una instancia de schedule para un tour en una fecha específica
     */
    private void createScheduleInstance(Tour tour, LocalDate date) {
        LocalTime startTime = tour.getDefaultStartTime() != null
                ? tour.getDefaultStartTime()
                : LocalTime.of(20, 0); // Default a las 20:00 si no está definido

        ZonedDateTime startDateTime = ZonedDateTime.of(date, startTime, ZONE_ID);
        Instant startInstant = startDateTime.toInstant();

        TourSchedule schedule = new TourSchedule();
        schedule.setTour(tour);
        schedule.setStartDatetime(startInstant);
        schedule.setMaxParticipants(tour.getDefaultMaxParticipants());
        schedule.setStatus("OPEN");

        tourScheduleRepository.save(schedule);

        logger.debug("Schedule creado para tour {} en {}", tour.getId(), startDateTime);
    }

    /**
     * Método público para trigger manual (útil para testing y admin panel)
     */
    @Transactional
    public void generateSchedulesManually() {
        logger.info("Generación manual de schedules iniciada por administrador");
        generateSchedulesAutomatically();
    }
}
