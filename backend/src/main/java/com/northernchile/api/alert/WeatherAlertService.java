package com.northernchile.api.alert;

import com.northernchile.api.external.LunarService;
import com.northernchile.api.external.WeatherService;
import com.northernchile.api.external.dto.DailyForecast;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.WeatherAlert;
import com.northernchile.api.tour.TourScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Servicio de alertas climáticas
 *
 * Se ejecuta diariamente para detectar cambios en el pronóstico que puedan
 * afectar schedules ya creados. Genera alertas para que el admin revise y decida.
 *
 * Escenario:
 * 1. Generador crea schedule para dentro de 5 días (clima OK)
 * 2. Usuario reserva el tour
 * 3. 3 días después: Pronóstico cambia, ahora hay tormenta
 * 4. Este servicio detecta el cambio y crea una alerta
 * 5. Admin ve la alerta en el dashboard y decide si cancelar
 */
@Service
public class WeatherAlertService {

    private static final Logger logger = LoggerFactory.getLogger(WeatherAlertService.class);
    private static final ZoneId ZONE_ID = ZoneId.of("America/Santiago");
    private static final int DAYS_TO_CHECK = 8; // Límite de OpenWeatherMap
    private static final double WIND_THRESHOLD_KNOTS = 25.0;
    private static final double WIND_THRESHOLD_MS = WIND_THRESHOLD_KNOTS * 0.514444; // Convertir a m/s
    private static final int CLOUD_THRESHOLD = 80; // %

    private final TourScheduleRepository scheduleRepository;
    private final WeatherAlertRepository alertRepository;
    private final WeatherService weatherService;
    private final LunarService lunarService;

    public WeatherAlertService(
            TourScheduleRepository scheduleRepository,
            WeatherAlertRepository alertRepository,
            WeatherService weatherService,
            LunarService lunarService) {
        this.scheduleRepository = scheduleRepository;
        this.alertRepository = alertRepository;
        this.weatherService = weatherService;
        this.lunarService = lunarService;
    }

    /**
     * Se ejecuta diariamente a las 3 AM (después del generador de schedules)
     * Busca schedules en los próximos 8 días y verifica si el pronóstico empeoró
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "America/Santiago")
    @Transactional
    public void checkWeatherAlertsAutomatically() {
        logger.info("Iniciando verificación de alertas climáticas");

        LocalDate today = LocalDate.now(ZONE_ID);
        LocalDate endDate = today.plusDays(DAYS_TO_CHECK);

        // Buscar schedules en los próximos 8 días que no estén cancelados
        ZonedDateTime startDateTime = today.atStartOfDay(ZONE_ID);
        ZonedDateTime endDateTime = endDate.atStartOfDay(ZONE_ID);

        List<TourSchedule> upcomingSchedules = scheduleRepository
                .findByStartDatetimeBetween(startDateTime, endDateTime)
                .stream()
                .filter(schedule -> !"CANCELLED".equals(schedule.getStatus()))
                .toList();

        logger.info("Encontrados {} schedules en los próximos {} días", upcomingSchedules.size(), DAYS_TO_CHECK);

        int alertsCreated = 0;

        for (TourSchedule schedule : upcomingSchedules) {
            try {
                LocalDate scheduleDate = schedule.getStartDatetime().atZone(ZONE_ID).toLocalDate();
                Tour tour = schedule.getTour();

                // Verificar si ya existe una alerta pendiente para este schedule
                boolean hasPendingAlert = !alertRepository
                        .findByTourScheduleIdAndStatus(schedule.getId(), "PENDING")
                        .isEmpty();

                if (hasPendingAlert) {
                    continue; // Ya hay una alerta pendiente, no crear otra
                }

                // Verificar condiciones problemáticas
                WeatherAlert alert = checkConditions(schedule, scheduleDate, tour);

                if (alert != null) {
                    alertRepository.save(alert);
                    alertsCreated++;
                    logger.info("Alerta creada para schedule {} (Tour: {}, Fecha: {}): {}",
                            schedule.getId(), tour.getId(), scheduleDate, alert.getMessage());
                }

            } catch (Exception e) {
                logger.error("Error verificando schedule {}: {}", schedule.getId(), e.getMessage());
            }
        }

        logger.info("Verificación completada. Alertas creadas: {}", alertsCreated);
    }

    /**
     * Verifica las condiciones climáticas y lunares para un schedule
     * @return WeatherAlert si hay problemas, null si todo OK
     */
    private WeatherAlert checkConditions(TourSchedule schedule, LocalDate date, Tour tour) {
        // Verificar viento para tours sensibles
        if (tour.isWindSensitive()) {
            DailyForecast forecast = weatherService.getDailyForecast(date);
            if (forecast != null) {
                double maxWind = forecast.windSpeed;
                if (forecast.windGust != null && forecast.windGust > maxWind) {
                    maxWind = forecast.windGust;
                }

                if (maxWind > WIND_THRESHOLD_MS) {
                    WeatherAlert alert = new WeatherAlert();
                    alert.setTourSchedule(schedule);
                    alert.setAlertType("WIND");
                    alert.setSeverity("CRITICAL");
                    alert.setWindSpeed(maxWind);
                    alert.setMessage(String.format(
                            "Viento excesivo detectado: %.1f m/s (%.1f nudos). Umbral: %.0f nudos",
                            maxWind, maxWind / 0.514444, WIND_THRESHOLD_KNOTS
                    ));
                    return alert;
                }
            }
        }

        // Verificar nubosidad para tours sensibles
        if (tour.isCloudSensitive()) {
            DailyForecast forecast = weatherService.getDailyForecast(date);
            if (forecast != null && forecast.clouds > CLOUD_THRESHOLD) {
                WeatherAlert alert = new WeatherAlert();
                alert.setTourSchedule(schedule);
                alert.setAlertType("CLOUDS");
                alert.setSeverity("WARNING");
                alert.setCloudCoverage(forecast.clouds);
                alert.setMessage(String.format(
                        "Nubosidad alta detectada: %d%%. Umbral: %d%%",
                        forecast.clouds, CLOUD_THRESHOLD
                ));
                return alert;
            }
        }

        // Verificar luna llena para tours astronómicos
        if (tour.isMoonSensitive()) {
            if (lunarService.isFullMoon(date)) {
                double moonPhase = lunarService.getMoonPhase(date);
                int illumination = lunarService.getMoonIllumination(date);

                WeatherAlert alert = new WeatherAlert();
                alert.setTourSchedule(schedule);
                alert.setAlertType("MOON");
                alert.setSeverity("CRITICAL");
                alert.setMoonPhase(moonPhase);
                alert.setMessage(String.format(
                        "Luna llena detectada: %d%% iluminación (fase: %.2f). Tours astronómicos afectados",
                        illumination, moonPhase
                ));
                return alert;
            }
        }

        return null; // Todo OK, no hay alerta
    }

    /**
     * Método manual para verificar alertas (útil para testing)
     */
    @Transactional
    public int checkWeatherAlertsManually() {
        logger.info("Verificación manual de alertas iniciada por administrador");
        checkWeatherAlertsAutomatically();
        return alertRepository.findByStatus("PENDING").size();
    }

    /**
     * Obtiene todas las alertas pendientes
     */
    public List<WeatherAlert> getPendingAlerts() {
        return alertRepository.findByStatus("PENDING");
    }

    /**
     * Cuenta alertas pendientes
     */
    public long countPendingAlerts() {
        return alertRepository.countByStatus("PENDING");
    }

    /**
     * Resuelve una alerta
     */
    @Transactional
    public void resolveAlert(String alertId, String resolution, String adminId) {
        WeatherAlert alert = alertRepository.findById(java.util.UUID.fromString(alertId))
                .orElseThrow(() -> new RuntimeException("Alert not found"));

        alert.setStatus("RESOLVED");
        alert.setResolution(resolution);
        alert.setResolvedBy(adminId);
        alert.setResolvedAt(Instant.now());

        alertRepository.save(alert);

        logger.info("Alerta {} resuelta por admin {}: {}", alertId, adminId, resolution);
    }
}
