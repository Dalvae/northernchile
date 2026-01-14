package com.northernchile.api.alert;

import com.northernchile.api.alert.dto.AlertCheckRes;
import com.northernchile.api.alert.dto.AlertCountRes;
import com.northernchile.api.alert.dto.AlertHistoryRes;
import com.northernchile.api.alert.dto.WeatherAlertMapper;
import com.northernchile.api.alert.dto.WeatherAlertRes;
import com.northernchile.api.common.dto.MessageRes;
import com.northernchile.api.config.security.annotation.CurrentUser;
import com.northernchile.api.model.User;
import com.northernchile.api.model.WeatherAlert;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestión de alertas climáticas
 * Solo accesible para SUPER_ADMIN y PARTNER_ADMIN
 */
@RestController
@RequestMapping("/api/admin/alerts")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')")
public class WeatherAlertController {

    private final WeatherAlertService alertService;
    private final WeatherAlertRepository alertRepository;
    private final WeatherAlertMapper alertMapper;

    public WeatherAlertController(
            WeatherAlertService alertService,
            WeatherAlertRepository alertRepository,
            WeatherAlertMapper alertMapper) {
        this.alertService = alertService;
        this.alertRepository = alertRepository;
        this.alertMapper = alertMapper;
    }

    /**
     * Obtiene todas las alertas pendientes
     */
    @GetMapping
    public List<WeatherAlertRes> getPendingAlerts() {
        List<WeatherAlert> alerts = alertRepository.findByStatusWithScheduleAndTour("PENDING");
        return alertMapper.toResList(alerts);
    }

    /**
     * Obtiene el conteo de alertas pendientes (para mostrar badge)
     */
    @GetMapping("/count")
    public AlertCountRes getPendingAlertsCount() {
        long count = alertService.countPendingAlerts();
        return new AlertCountRes(count);
    }

    /**
     * Obtiene una alerta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<WeatherAlertRes> getAlertById(@PathVariable String id) {
        return alertRepository.findByIdWithScheduleAndTour(java.util.UUID.fromString(id))
                .map(alertMapper::toRes)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene alertas para un schedule específico
     */
    @GetMapping("/schedule/{scheduleId}")
    public List<WeatherAlertRes> getAlertsBySchedule(@PathVariable String scheduleId) {
        List<WeatherAlert> alerts = alertRepository.findByScheduleIdWithTour(java.util.UUID.fromString(scheduleId));
        return alertMapper.toResList(alerts);
    }

    /**
     * Resuelve una alerta
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<MessageRes> resolveAlert(
            @PathVariable String id,
            @jakarta.validation.Valid @RequestBody ResolveAlertRequest request,
            @CurrentUser User currentUser) {
        alertService.resolveAlert(id, request.resolution(), currentUser.getId().toString());
        return ResponseEntity.ok(MessageRes.of("Alert resolved successfully"));
    }

    /**
     * Trigger manual de verificación de alertas (para testing)
     */
    @PostMapping("/check")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AlertCheckRes> checkAlertsManually() {
        int pendingCount = alertService.checkWeatherAlertsManually();
        return ResponseEntity.ok(new AlertCheckRes("Verificación completada", pendingCount));
    }

    /**
     * Obtiene historial de alertas (resueltas + pendientes)
     */
    @GetMapping("/history")
    public AlertHistoryRes getAlertsHistory() {
        List<WeatherAlert> alerts = alertRepository.findAllWithScheduleAndTour();
        return alertMapper.toHistoryRes(alerts);
    }

    record ResolveAlertRequest(String resolution) {}
}
