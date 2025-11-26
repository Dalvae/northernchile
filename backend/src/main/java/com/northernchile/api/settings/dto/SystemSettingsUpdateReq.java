package com.northernchile.api.settings.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record SystemSettingsUpdateReq(
    @Valid WeatherAlertSettings weatherAlerts,
    @Valid BookingSettings bookingSettings,
    @Valid AstronomicalSettings astronomicalTours,
    @Valid NotificationSettings notifications,
    @Valid PaymentSettings payments
) {
    public record WeatherAlertSettings(
        @Min(1) @Max(100) Integer windThreshold,
        @Min(0) @Max(100) Integer cloudCoverThreshold,
        @Min(0) @Max(100) Integer rainProbabilityThreshold,
        @Min(1) @Max(24) Integer checkFrequency
    ) {}

    public record BookingSettings(
        @Min(1) @Max(168) Integer cancellationWindowHours,
        @Min(1) @Max(365) Integer maxAdvanceBookingDays,
        @Min(1) @Max(72) Integer minAdvanceBookingHours,
        Boolean autoConfirmBookings
    ) {}

    public record AstronomicalSettings(
        @Min(0) @Max(100) Integer moonIlluminationThreshold,
        Boolean autoBlockFullMoon,
        @Min(1) @Max(180) Integer scheduleGenerationDaysAhead
    ) {}

    public record NotificationSettings(
        Boolean emailEnabled,
        Boolean smsEnabled,
        Boolean sendBookingConfirmation,
        Boolean sendCancellationNotice,
        Boolean sendWeatherAlerts
    ) {}

    public record PaymentSettings(
        Boolean mockPaymentMode,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @Min(0) @Max(100) Integer taxRate,
        @Min(0) @Max(100) Integer depositPercentage
    ) {}
}
