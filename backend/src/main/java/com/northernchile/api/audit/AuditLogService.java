package com.northernchile.api.audit;

import com.northernchile.api.model.AuditLog;
import com.northernchile.api.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Log an admin action
     * @param user The user performing the action
     * @param action Action performed (CREATE, UPDATE, DELETE, RESTORE, etc.)
     * @param entityType Type of entity (TOUR, USER, BOOKING, SCHEDULE, etc.)
     * @param entityId ID of the entity
     * @param entityDescription Human-readable description of the entity
     * @param oldValues Snapshot of entity before change (for UPDATE/DELETE)
     * @param newValues Snapshot of entity after change (for CREATE/UPDATE)
     */
    @Transactional
    public void logAction(
            User user,
            String action,
            String entityType,
            UUID entityId,
            String entityDescription,
            Map<String, Object> oldValues,
            Map<String, Object> newValues
    ) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setUserEmail(user.getEmail());
        auditLog.setUserRole(user.getRole());
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setEntityDescription(entityDescription);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);

        // Try to get request context for IP and User-Agent
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIp(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // If we can't get request context, just skip it
        }

        auditLogRepository.save(auditLog);
    }

    /**
     * Simplified logging for DELETE operations
     */
    @Transactional
    public void logDelete(User user, String entityType, UUID entityId, String entityDescription, Map<String, Object> oldValues) {
        logAction(user, "DELETE", entityType, entityId, entityDescription, oldValues, null);
    }

    /**
     * Simplified logging for CREATE operations
     */
    @Transactional
    public void logCreate(User user, String entityType, UUID entityId, String entityDescription, Map<String, Object> newValues) {
        logAction(user, "CREATE", entityType, entityId, entityDescription, null, newValues);
    }

    /**
     * Simplified logging for UPDATE operations
     */
    @Transactional
    public void logUpdate(User user, String entityType, UUID entityId, String entityDescription, Map<String, Object> oldValues, Map<String, Object> newValues) {
        logAction(user, "UPDATE", entityType, entityId, entityDescription, oldValues, newValues);
    }

    /**
     * Simplified logging for RESTORE operations (soft delete undo)
     */
    @Transactional
    public void logRestore(User user, String entityType, UUID entityId, String entityDescription) {
        logAction(user, "RESTORE", entityType, entityId, entityDescription, null, null);
    }

    /**
     * Get client IP address from request, handling proxies
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
