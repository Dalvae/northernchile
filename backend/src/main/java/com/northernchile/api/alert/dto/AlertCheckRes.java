package com.northernchile.api.alert.dto;

/**
 * Response DTO for manual alert check.
 */
public record AlertCheckRes(
    String message,
    int pendingAlerts
) {}
