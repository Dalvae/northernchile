package com.northernchile.api.settings;

import com.northernchile.api.settings.dto.SystemSettingsRes;
import com.northernchile.api.settings.dto.SystemSettingsUpdateReq;
import org.springframework.stereotype.Service;

/**
 * Sistema de configuración en memoria.
 * NOTA: En producción esto debería usar una tabla de base de datos
 * o un sistema de configuración centralizado como Spring Cloud Config.
 */
@Service
public class SystemSettingsService {

    private SystemSettingsRes currentSettings;

    public SystemSettingsService() {
        this.currentSettings = getDefaultSettings();
    }

    public SystemSettingsRes getCurrentSettings() {
        return currentSettings;
    }

    public SystemSettingsRes updateSettings(SystemSettingsUpdateReq updateReq) {
        // Since Records are immutable, we create a new instance with updated values
        // Merge with existing settings
        
        SystemSettingsRes.WeatherAlertSettingsRes weatherAlerts = updateReq.weatherAlerts() != null
            ? new SystemSettingsRes.WeatherAlertSettingsRes(
                updateReq.weatherAlerts().windThreshold(),
                updateReq.weatherAlerts().cloudCoverThreshold(),
                updateReq.weatherAlerts().rainProbabilityThreshold(),
                updateReq.weatherAlerts().checkFrequency()
            )
            : currentSettings.weatherAlerts();

        SystemSettingsRes.BookingSettingsRes bookingSettings = updateReq.bookingSettings() != null
            ? new SystemSettingsRes.BookingSettingsRes(
                updateReq.bookingSettings().cancellationWindowHours(),
                updateReq.bookingSettings().maxAdvanceBookingDays(),
                updateReq.bookingSettings().minAdvanceBookingHours(),
                updateReq.bookingSettings().autoConfirmBookings()
            )
            : currentSettings.bookingSettings();

        SystemSettingsRes.AstronomicalSettingsRes astronomicalTours = updateReq.astronomicalTours() != null
            ? new SystemSettingsRes.AstronomicalSettingsRes(
                updateReq.astronomicalTours().moonIlluminationThreshold(),
                updateReq.astronomicalTours().autoBlockFullMoon(),
                updateReq.astronomicalTours().scheduleGenerationDaysAhead()
            )
            : currentSettings.astronomicalTours();

        SystemSettingsRes.NotificationSettingsRes notifications = updateReq.notifications() != null
            ? new SystemSettingsRes.NotificationSettingsRes(
                updateReq.notifications().emailEnabled(),
                updateReq.notifications().smsEnabled(),
                updateReq.notifications().sendBookingConfirmation(),
                updateReq.notifications().sendCancellationNotice(),
                updateReq.notifications().sendWeatherAlerts()
            )
            : currentSettings.notifications();

        SystemSettingsRes.PaymentSettingsRes payments = updateReq.payments() != null
            ? new SystemSettingsRes.PaymentSettingsRes(
                updateReq.payments().mockPaymentMode(),
                updateReq.payments().currency(),
                updateReq.payments().taxRate(),
                updateReq.payments().depositPercentage()
            )
            : currentSettings.payments();

        currentSettings = new SystemSettingsRes(
            weatherAlerts,
            bookingSettings,
            astronomicalTours,
            notifications,
            payments
        );

        return currentSettings;
    }

    private SystemSettingsRes getDefaultSettings() {
        SystemSettingsRes.WeatherAlertSettingsRes weatherAlerts = 
            new SystemSettingsRes.WeatherAlertSettingsRes(25, 80, 50, 6);

        SystemSettingsRes.BookingSettingsRes bookingSettings = 
            new SystemSettingsRes.BookingSettingsRes(24, 90, 6, false);

        SystemSettingsRes.AstronomicalSettingsRes astronomicalTours = 
            new SystemSettingsRes.AstronomicalSettingsRes(90, true, 90);

        SystemSettingsRes.NotificationSettingsRes notifications = 
            new SystemSettingsRes.NotificationSettingsRes(true, false, true, true, true);

        SystemSettingsRes.PaymentSettingsRes payments = 
            new SystemSettingsRes.PaymentSettingsRes(true, "CLP", 19, 0);

        return new SystemSettingsRes(
            weatherAlerts,
            bookingSettings,
            astronomicalTours,
            notifications,
            payments
        );
    }
}
