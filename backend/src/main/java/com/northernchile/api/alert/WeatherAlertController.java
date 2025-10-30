package com.northernchile.api.alert;

import com.northernchile.api.model.WeatherAlert;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    public WeatherAlertController(WeatherAlertService alertService, WeatherAlertRepository alertRepository) {
        this.alertService = alertService;
        this.alertRepository = alertRepository;
    }

    /**
     * Obtiene todas las alertas pendientes
     */
    @GetMapping
    public List<WeatherAlert> getPendingAlerts() {
        return alertService.getPendingAlerts();
    }

    /**
     * Obtiene el conteo de alertas pendientes (para mostrar badge)
     */
    @GetMapping("/count")
    public Map<String, Long> getPendingAlertsCount() {
        long count = alertService.countPendingAlerts();
        return Map.of("pending", count);
    }

    /**
     * Obtiene una alerta por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<WeatherAlert> getAlertById(@PathVariable String id) {
        return alertRepository.findById(java.util.UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene alertas para un schedule específico
     */
    @GetMapping("/schedule/{scheduleId}")
    public List<WeatherAlert> getAlertsBySchedule(@PathVariable String scheduleId) {
        return alertRepository.findByTourScheduleId(java.util.UUID.fromString(scheduleId));
    }

    /**
     * Resuelve una alerta
     */
    @PostMapping("/{id}/resolve")
    public ResponseEntity<Map<String, String>> resolveAlert(
            @PathVariable String id,
            @RequestBody ResolveAlertRequest request) {
        try {
            // TODO: Obtener adminId del usuario autenticado
            String adminId = "admin-temp"; // Por ahora hardcoded
            alertService.resolveAlert(id, request.resolution(), adminId);
            return ResponseEntity.ok(Map.of("message", "Alert resolved successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Trigger manual de verificación de alertas (para testing)
     */
    @PostMapping("/check")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> checkAlertsManually() {
        int pendingCount = alertService.checkWeatherAlertsManually();
        return ResponseEntity.ok(Map.of(
                "message", "Verificación completada",
                "pendingAlerts", pendingCount
        ));
    }

    /**
     * Obtiene historial de alertas (resueltas + pendientes)
     */
    @GetMapping("/history")
    public List<WeatherAlert> getAlertsHistory() {
        return alertRepository.findAll();
    }

    record ResolveAlertRequest(String resolution) {}
}
