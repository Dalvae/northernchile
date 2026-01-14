package com.northernchile.api.audit.dto;

import java.util.List;

/**
 * Response DTO for paginated audit logs.
 */
public record AuditLogPageRes(
    List<AuditLogRes> logs,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize
) {}
