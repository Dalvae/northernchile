package com.northernchile.api.model;

/**
 * Status values for weather alerts.
 *
 * - PENDING: Alert created, waiting for admin review
 * - REVIEWED: Admin has reviewed but not yet resolved
 * - RESOLVED: Alert has been resolved (either cancelled schedule or marked as safe)
 */
public enum WeatherAlertStatus {
    PENDING,
    REVIEWED,
    RESOLVED
}
