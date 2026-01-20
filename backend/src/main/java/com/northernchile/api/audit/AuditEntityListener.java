package com.northernchile.api.audit;

import com.northernchile.api.model.User;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JPA Entity Listener for automatic audit logging.
 *
 * This listener intercepts JPA lifecycle events (persist, update, remove) and
 * automatically creates audit log entries, eliminating the need for manual
 * auditLogService calls in service classes.
 *
 * How it works:
 * 1. Entity implements AuditableEntity interface
 * 2. Entity annotated with @EntityListeners(AuditEntityListener.class)
 * 3. AuditContextFilter sets the current user before each request
 * 4. On CREATE/UPDATE/DELETE, this listener logs the action automatically
 *
 * Note: JPA listeners are not Spring beans, so we use static ApplicationContext
 * access to get the AuditLogService. This is a standard pattern for JPA listeners.
 */
@Component
public class AuditEntityListener implements ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(AuditEntityListener.class);

    // Static reference to Spring's ApplicationContext
    private static ApplicationContext applicationContext;

    // Thread-local storage for old values (captured in @PreUpdate)
    private static final ThreadLocal<Map<Object, Map<String, Object>>> oldValuesMap =
        ThreadLocal.withInitial(ConcurrentHashMap::new);

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * Called after a new entity is persisted.
     */
    @PostPersist
    public void onPostPersist(Object entity) {
        if (!(entity instanceof AuditableEntity auditable)) {
            return;
        }

        User currentUser = AuditContext.getCurrentUser();
        if (currentUser == null) {
            log.debug("No user in AuditContext for CREATE of {} - skipping audit",
                auditable.getAuditEntityType());
            return;
        }

        try {
            AuditLogService auditLogService = getAuditLogService();
            if (auditLogService == null) {
                log.warn("AuditLogService not available - skipping audit for CREATE");
                return;
            }

            auditLogService.logCreate(
                currentUser,
                auditable.getAuditEntityType(),
                auditable.getId(),
                auditable.getAuditDescription(),
                auditable.getAuditSnapshot()
            );

            log.debug("Auto-audited CREATE for {} id={}", auditable.getAuditEntityType(), auditable.getId());

        } catch (Exception e) {
            // Don't let audit failures break the main operation
            log.error("Failed to auto-audit CREATE for {} id={}: {}",
                auditable.getAuditEntityType(), auditable.getId(), e.getMessage());
        }
    }

    /**
     * Called before an entity is updated.
     * Captures old values for comparison in @PostUpdate.
     */
    @PreUpdate
    public void onPreUpdate(Object entity) {
        if (!(entity instanceof AuditableEntity auditable)) {
            return;
        }

        // Capture old values before the update
        Map<String, Object> snapshot = auditable.getAuditSnapshot();
        if (snapshot != null) {
            oldValuesMap.get().put(entity, snapshot);
        }
    }

    /**
     * Called after an entity is updated.
     */
    @PostUpdate
    public void onPostUpdate(Object entity) {
        if (!(entity instanceof AuditableEntity auditable)) {
            return;
        }

        User currentUser = AuditContext.getCurrentUser();
        if (currentUser == null) {
            log.debug("No user in AuditContext for UPDATE of {} - skipping audit",
                auditable.getAuditEntityType());
            cleanupOldValues(entity);
            return;
        }

        try {
            AuditLogService auditLogService = getAuditLogService();
            if (auditLogService == null) {
                log.warn("AuditLogService not available - skipping audit for UPDATE");
                cleanupOldValues(entity);
                return;
            }

            // Get old values captured in @PreUpdate
            Map<String, Object> oldValues = oldValuesMap.get().remove(entity);
            Map<String, Object> newValues = auditable.getAuditSnapshot();

            auditLogService.logUpdate(
                currentUser,
                auditable.getAuditEntityType(),
                auditable.getId(),
                auditable.getAuditDescription(),
                oldValues,
                newValues
            );

            log.debug("Auto-audited UPDATE for {} id={}", auditable.getAuditEntityType(), auditable.getId());

        } catch (Exception e) {
            // Don't let audit failures break the main operation
            log.error("Failed to auto-audit UPDATE for {} id={}: {}",
                auditable.getAuditEntityType(), auditable.getId(), e.getMessage());
        } finally {
            cleanupOldValues(entity);
        }
    }

    /**
     * Called after an entity is removed.
     */
    @PostRemove
    public void onPostRemove(Object entity) {
        if (!(entity instanceof AuditableEntity auditable)) {
            return;
        }

        User currentUser = AuditContext.getCurrentUser();
        if (currentUser == null) {
            log.debug("No user in AuditContext for DELETE of {} - skipping audit",
                auditable.getAuditEntityType());
            return;
        }

        try {
            AuditLogService auditLogService = getAuditLogService();
            if (auditLogService == null) {
                log.warn("AuditLogService not available - skipping audit for DELETE");
                return;
            }

            auditLogService.logDelete(
                currentUser,
                auditable.getAuditEntityType(),
                auditable.getId(),
                auditable.getAuditDescription(),
                auditable.getAuditSnapshot()
            );

            log.debug("Auto-audited DELETE for {} id={}", auditable.getAuditEntityType(), auditable.getId());

        } catch (Exception e) {
            // Don't let audit failures break the main operation
            log.error("Failed to auto-audit DELETE for {} id={}: {}",
                auditable.getAuditEntityType(), auditable.getId(), e.getMessage());
        }
    }

    /**
     * Gets the AuditLogService from Spring context.
     */
    private AuditLogService getAuditLogService() {
        if (applicationContext == null) {
            return null;
        }
        try {
            return applicationContext.getBean(AuditLogService.class);
        } catch (BeansException e) {
            return null;
        }
    }

    /**
     * Cleans up old values from thread-local storage.
     */
    private void cleanupOldValues(Object entity) {
        oldValuesMap.get().remove(entity);
    }
}
