package com.northernchile.api.audit.dto;

/**
 * Response DTO for audit statistics.
 */
public record AuditStatsRes(
    long totalLogs,
    long createActions,
    long updateActions,
    long deleteActions
) {}
