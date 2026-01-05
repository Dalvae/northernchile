package com.northernchile.api.settings.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SystemSettingsRes(
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) WeatherAlertSettingsRes weatherAlerts,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) BookingSettingsRes bookingSettings,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) AstronomicalSettingsRes astronomicalTours,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) NotificationSettingsRes notifications,
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED) PaymentSettingsRes payments
) {
    public record WeatherAlertSettingsRes(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer windThreshold,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer cloudCoverThreshold,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer rainProbabilityThreshold,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer checkFrequency
    ) {}

    public record BookingSettingsRes(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer cancellationWindowHours,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer maxAdvanceBookingDays,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer minAdvanceBookingHours,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Boolean autoConfirmBookings
    ) {}

    public record AstronomicalSettingsRes(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer moonIlluminationThreshold,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Boolean autoBlockFullMoon,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer scheduleGenerationDaysAhead
    ) {}

    public record NotificationSettingsRes(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Boolean emailEnabled,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Boolean smsEnabled,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Boolean sendBookingConfirmation,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Boolean sendCancellationNotice,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Boolean sendWeatherAlerts
    ) {}

    public record PaymentSettingsRes(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Boolean mockPaymentMode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String currency,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer taxRate,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Integer depositPercentage
    ) {}
}
