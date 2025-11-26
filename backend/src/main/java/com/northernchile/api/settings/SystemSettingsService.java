package com.northernchile.api.settings;

import com.northernchile.api.settings.dto.SystemSettingsUpdateReq;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Sistema de configuración en memoria.
 * NOTA: En producción esto debería usar una tabla de base de datos
 * o un sistema de configuración centralizado como Spring Cloud Config.
 */
@Service
public class SystemSettingsService {

    private Map<String, Object> currentSettings;

    public SystemSettingsService() {
        this.currentSettings = getDefaultSettings();
    }

    public Map<String, Object> getCurrentSettings() {
        return new HashMap<>(currentSettings);
    }

    public Map<String, Object> updateSettings(SystemSettingsUpdateReq updateReq) {
        // Update in-memory settings
        Map<String, Object> newSettings = new HashMap<>();

        if (updateReq.weatherAlerts() != null) {
            Map<String, Object> weatherAlerts = new HashMap<>();
            weatherAlerts.put("windThreshold", updateReq.weatherAlerts().windThreshold());
            weatherAlerts.put("cloudCoverThreshold", updateReq.weatherAlerts().cloudCoverThreshold());
            weatherAlerts.put("rainProbabilityThreshold", updateReq.weatherAlerts().rainProbabilityThreshold());
            weatherAlerts.put("checkFrequency", updateReq.weatherAlerts().checkFrequency());
            newSettings.put("weatherAlerts", weatherAlerts);
        }

        if (updateReq.bookingSettings() != null) {
            Map<String, Object> bookingSettings = new HashMap<>();
            bookingSettings.put("cancellationWindowHours", updateReq.bookingSettings().cancellationWindowHours());
            bookingSettings.put("maxAdvanceBookingDays", updateReq.bookingSettings().maxAdvanceBookingDays());
            bookingSettings.put("minAdvanceBookingHours", updateReq.bookingSettings().minAdvanceBookingHours());
            bookingSettings.put("autoConfirmBookings", updateReq.bookingSettings().autoConfirmBookings());
            newSettings.put("bookingSettings", bookingSettings);
        }

        if (updateReq.astronomicalTours() != null) {
            Map<String, Object> astronomicalTours = new HashMap<>();
            astronomicalTours.put("moonIlluminationThreshold", updateReq.astronomicalTours().moonIlluminationThreshold());
            astronomicalTours.put("autoBlockFullMoon", updateReq.astronomicalTours().autoBlockFullMoon());
            astronomicalTours.put("scheduleGenerationDaysAhead", updateReq.astronomicalTours().scheduleGenerationDaysAhead());
            newSettings.put("astronomicalTours", astronomicalTours);
        }

        if (updateReq.notifications() != null) {
            Map<String, Object> notifications = new HashMap<>();
            notifications.put("emailEnabled", updateReq.notifications().emailEnabled());
            notifications.put("smsEnabled", updateReq.notifications().smsEnabled());
            notifications.put("sendBookingConfirmation", updateReq.notifications().sendBookingConfirmation());
            notifications.put("sendCancellationNotice", updateReq.notifications().sendCancellationNotice());
            notifications.put("sendWeatherAlerts", updateReq.notifications().sendWeatherAlerts());
            newSettings.put("notifications", notifications);
        }

        if (updateReq.payments() != null) {
            Map<String, Object> payments = new HashMap<>();
            payments.put("mockPaymentMode", updateReq.payments().mockPaymentMode());
            payments.put("currency", updateReq.payments().currency());
            payments.put("taxRate", updateReq.payments().taxRate());
            payments.put("depositPercentage", updateReq.payments().depositPercentage());
            newSettings.put("payments", payments);
        }

        this.currentSettings = newSettings;
        return getCurrentSettings();
    }

    private Map<String, Object> getDefaultSettings() {
        Map<String, Object> settings = new HashMap<>();

        Map<String, Object> weatherAlerts = new HashMap<>();
        weatherAlerts.put("windThreshold", 25);
        weatherAlerts.put("cloudCoverThreshold", 80);
        weatherAlerts.put("rainProbabilityThreshold", 50);
        weatherAlerts.put("checkFrequency", 6);
        settings.put("weatherAlerts", weatherAlerts);

        Map<String, Object> bookingSettings = new HashMap<>();
        bookingSettings.put("cancellationWindowHours", 24);
        bookingSettings.put("maxAdvanceBookingDays", 90);
        bookingSettings.put("minAdvanceBookingHours", 6);
        bookingSettings.put("autoConfirmBookings", false);
        settings.put("bookingSettings", bookingSettings);

        Map<String, Object> astronomicalTours = new HashMap<>();
        astronomicalTours.put("moonIlluminationThreshold", 90);
        astronomicalTours.put("autoBlockFullMoon", true);
        astronomicalTours.put("scheduleGenerationDaysAhead", 90);
        settings.put("astronomicalTours", astronomicalTours);

        Map<String, Object> notifications = new HashMap<>();
        notifications.put("emailEnabled", true);
        notifications.put("smsEnabled", false);
        notifications.put("sendBookingConfirmation", true);
        notifications.put("sendCancellationNotice", true);
        notifications.put("sendWeatherAlerts", true);
        settings.put("notifications", notifications);

        Map<String, Object> payments = new HashMap<>();
        payments.put("mockPaymentMode", true);
        payments.put("currency", "CLP");
        payments.put("taxRate", 19);
        payments.put("depositPercentage", 0);
        settings.put("payments", payments);

        return settings;
    }
}
