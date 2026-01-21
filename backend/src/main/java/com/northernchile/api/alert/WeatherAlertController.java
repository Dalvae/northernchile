package com.northernchile.api.alert;

import com.northernchile.api.alert.dto.AlertCheckRes;
import com.northernchile.api.alert.dto.AlertCountRes;
import com.northernchile.api.alert.dto.AlertHistoryRes;
import com.northernchile.api.alert.dto.WeatherAlertMapper;
import com.northernchile.api.alert.dto.WeatherAlertRes;
import com.northernchile.api.booking.dto.ScheduleCancellationResult;
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
        List<WeatherAlert> alerts = alertRepository.findByStatusWithScheduleAndTour(WeatherAlertStatus.PENDING);
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
     * Resuelve una alerta (simple resolution, no cascade cancellation)
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
     * Resuelve una alerta y opcionalmente cancela el schedule con refunds en cascada.
     * Use cancelWithRefunds=true to cancel the tour and automatically refund all bookings.
     *
     * @param id Alert ID
     * @param cancelWithRefunds If true, cancels the schedule and processes refunds
     * @return ScheduleCancellationResult with details of refunds processed (if cancelled)
     */
    @PostMapping("/{id}/resolve-with-action")
    public ResponseEntity<?> resolveAlertWithAction(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean cancelWithRefunds,
            @CurrentUser User currentUser) {

        ScheduleCancellationResult result = alertService.resolveAlertWithAction(
                id,
                cancelWithRefunds,
                currentUser
        );

        if (result != null) {
            // Cancelled with refunds - return the full result
            return ResponseEntity.ok(result);
        } else {
            // Just resolved without cancellation
            return ResponseEntity.ok(MessageRes.of("Alert resolved - tour kept"));
        }
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
