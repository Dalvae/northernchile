package com.northernchile.api.audit.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for a single audit log entry.
 */
public record AuditLogRes(
    UUID id,
    String userEmail,
    String userRole,
    String action,
    String entityType,
    UUID entityId,
    String entityDescription,
    Map<String, Object> oldValues,
    Map<String, Object> newValues,
    String ipAddress,
    String userAgent,
    Instant createdAt
) {
    /**
     * Factory method to convert from entity.
     */
    public static AuditLogRes from(com.northernchile.api.model.AuditLog entity) {
        return new AuditLogRes(
            entity.getId(),
            entity.getUserEmail(),
            entity.getUserRole(),
            entity.getAction(),
            entity.getEntityType(),
            entity.getEntityId(),
            entity.getEntityDescription(),
            entity.getOldValues(),
            entity.getNewValues(),
            entity.getIpAddress(),
            entity.getUserAgent(),
            entity.getCreatedAt()
        );
    }
}
