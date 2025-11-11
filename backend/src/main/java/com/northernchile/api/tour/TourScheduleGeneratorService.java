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

    // Ventana fija de generación para todos los tours
    // Siempre intentamos tener hasta 90 días de schedules futuros.
    private static final int DAYS_AHEAD_ALL = 90;

    // Ventana "cercana" donde sí aplicamos filtros de pronóstico duro
    private static final int HARD_WEATHER_WINDOW_DAYS = 2;

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
            // Determinar límite de días hacia adelante
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
     * Determina cuántos días adelante generar schedules.
     * Ahora usamos una ventana fija para simplificar: siempre 90 días.
     */
    private int getDaysToGenerate(Tour tour) {
        return DAYS_AHEAD_ALL;
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

        // Restricción por luminosidad lunar para tours sensibles a la luna
        // En lugar de solo "día exacto de luna llena", usamos umbral por iluminación,
        // lo que cubre de forma natural los días alrededor de la luna llena.
        if (tour.isMoonSensitive()) {
            int illumination = lunarService.getMoonIllumination(date);
            if (illumination >= 80) { // Umbral configurable si es necesario
                logger.debug("Fecha {} omitida para tour {} por luna brillante ({}%)", date, tour.getId(), illumination);
                return false;
            }
        }

        // Restricciones de clima solo en ventana muy cercana donde el pronóstico es fiable.
        // Más allá de HARD_WEATHER_WINDOW_DAYS, generamos igual y dejamos que Weather Alerts
        // se encargue de marcar riesgos sin bloquear la oferta.
        if (daysFromNow <= HARD_WEATHER_WINDOW_DAYS) {
            // Restricción de viento para tours sensibles al viento
            if (tour.isWindSensitive() && weatherService.isWindAboveThreshold(date, WIND_THRESHOLD_KNOTS)) {
                logger.debug("Fecha {} omitida para tour {} por viento excesivo (ventana cercana)", date, tour.getId());
                return false;
            }

            // Restricción de nubosidad para tours sensibles a las nubes
            if (tour.isCloudSensitive() && weatherService.isCloudyDay(date)) {
                logger.debug("Fecha {} omitida para tour {} por nubosidad excesiva (ventana cercana)", date, tour.getId());
                return false;
            }
        }


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
