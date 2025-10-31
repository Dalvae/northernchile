package com.northernchile.api.settings;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SystemSettingsController {

    /**
     * GET /api/admin/settings
     * Obtiene las configuraciones del sistema
     *
     * NOTA: Esta es una implementación simplificada.
     * En producción, estas configuraciones deberían almacenarse en una tabla de base de datos
     * o en un sistema de configuración centralizado como Spring Cloud Config.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        Map<String, Object> settings = new HashMap<>();

        // Configuraciones de alertas climáticas
        Map<String, Object> weatherAlerts = new HashMap<>();
        weatherAlerts.put("windThreshold", 25); // nudos
        weatherAlerts.put("cloudCoverThreshold", 80); // porcentaje
        weatherAlerts.put("rainProbabilityThreshold", 50); // porcentaje
        weatherAlerts.put("checkFrequency", 6); // horas
        settings.put("weatherAlerts", weatherAlerts);

        // Configuraciones de reservas
        Map<String, Object> bookingSettings = new HashMap<>();
        bookingSettings.put("cancellationWindowHours", 24); // horas antes del tour
        bookingSettings.put("maxAdvanceBookingDays", 90); // días hacia adelante
        bookingSettings.put("minAdvanceBookingHours", 6); // horas mínimas de anticipación
        bookingSettings.put("autoConfirmBookings", false); // confirmar automáticamente
        settings.put("bookingSettings", bookingSettings);

        // Configuraciones de tours astronómicos
        Map<String, Object> astronomicalTours = new HashMap<>();
        astronomicalTours.put("moonIlluminationThreshold", 90); // porcentaje
        astronomicalTours.put("autoBlockFullMoon", true); // bloquear automáticamente
        astronomicalTours.put("scheduleGenerationDaysAhead", 90); // días hacia adelante
        settings.put("astronomicalTours", astronomicalTours);

        // Configuraciones de notificaciones
        Map<String, Object> notifications = new HashMap<>();
        notifications.put("emailEnabled", true);
        notifications.put("smsEnabled", false);
        notifications.put("sendBookingConfirmation", true);
        notifications.put("sendCancellationNotice", true);
        notifications.put("sendWeatherAlerts", true);
        settings.put("notifications", notifications);

        // Configuraciones de pagos
        Map<String, Object> payments = new HashMap<>();
        payments.put("mockPaymentMode", true); // modo simulación
        payments.put("currency", "CLP");
        payments.put("taxRate", 19); // IVA en Chile
        payments.put("depositPercentage", 0); // porcentaje de depósito inicial
        settings.put("payments", payments);

        return ResponseEntity.ok(settings);
    }

    /**
     * PUT /api/admin/settings
     * Actualiza las configuraciones del sistema
     *
     * NOTA: Esta es una implementación placeholder.
     * En producción, esto debería validar y persistir los cambios en base de datos.
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody Map<String, Object> settings) {
        // TODO: Implementar validación y persistencia
        // Por ahora solo retornamos las configuraciones recibidas
        return ResponseEntity.ok(settings);
    }
}
