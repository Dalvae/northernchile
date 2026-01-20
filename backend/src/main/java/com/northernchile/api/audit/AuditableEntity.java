package com.northernchile.api.audit;

import java.util.Map;
import java.util.UUID;

/**
 * Interface for entities that should be automatically audited.
 *
 * Entities implementing this interface will have their CREATE, UPDATE, and DELETE
 * operations automatically logged to the audit log via AuditEntityListener.
 *
 * This eliminates the need for manual auditLogService calls scattered throughout
 * service classes, ensuring:
 * - Consistent auditing across all operations
 * - No forgotten audit calls
 * - Less boilerplate code in services
 *
 * Implementation note: Use @EntityListeners(AuditEntityListener.class) on entities
 * that implement this interface.
 */
public interface AuditableEntity {

    /**
     * Returns the entity's unique identifier.
     */
    UUID getId();

    /**
     * Returns a human-readable description for audit logging.
     * This should identify the entity in a meaningful way.
     *
     * Examples:
     * - Tour: "Astronomía Nocturna"
     * - Booking: "Tour X - John Doe"
     * - User: "john@example.com"
     *
     * @return Human-readable description
     */
    String getAuditDescription();

    /**
     * Returns the entity type name for audit logging.
     * This should be a consistent identifier across all operations.
     *
     * Examples: "TOUR", "BOOKING", "USER", "SCHEDULE"
     *
     * @return Entity type name
     */
    String getAuditEntityType();

    /**
     * Returns a snapshot of key values for audit logging.
     * This is used to capture old/new values during updates.
     *
     * Override this to include specific fields that should be tracked.
     * By default, returns null which means no field-level tracking.
     *
     * @return Map of field names to values, or null for no tracking
     */
    default Map<String, Object> getAuditSnapshot() {
        return null;
    }
}
