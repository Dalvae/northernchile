package com.northernchile.api.audit;

import com.northernchile.api.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para consultar el audit log
 * Solo accesible para SUPER_ADMIN
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * GET /api/admin/audit-logs?page=0&size=20&action=CREATE&entityType=TOUR&userEmail=admin@example.com
     * Obtiene logs de auditoría con paginación y filtros opcionales
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String userEmail) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> auditPage;

        // Apply filters if provided
        if (action != null && entityType != null && userEmail != null) {
            auditPage = auditLogRepository.findByActionAndEntityTypeAndUserEmail(action, entityType, userEmail, pageable);
        } else if (action != null && entityType != null) {
            auditPage = auditLogRepository.findByActionAndEntityType(action, entityType, pageable);
        } else if (action != null) {
            auditPage = auditLogRepository.findByAction(action, pageable);
        } else if (entityType != null) {
            auditPage = auditLogRepository.findByEntityType(entityType, pageable);
        } else if (userEmail != null) {
            auditPage = auditLogRepository.findByUserEmail(userEmail, pageable);
        } else {
            auditPage = auditLogRepository.findAll(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", auditPage.getContent());
        response.put("currentPage", auditPage.getNumber());
        response.put("totalItems", auditPage.getTotalElements());
        response.put("totalPages", auditPage.getTotalPages());

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/admin/audit-logs/{id}
     * Obtiene un log específico por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable String id) {
        return auditLogRepository.findById(java.util.UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/admin/audit-logs/stats
     * Obtiene estadísticas del audit log
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAuditStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalLogs = auditLogRepository.count();
        long createActions = auditLogRepository.countByAction("CREATE");
        long updateActions = auditLogRepository.countByAction("UPDATE");
        long deleteActions = auditLogRepository.countByAction("DELETE");

        stats.put("totalLogs", totalLogs);
        stats.put("createActions", createActions);
        stats.put("updateActions", updateActions);
        stats.put("deleteActions", deleteActions);

        return ResponseEntity.ok(stats);
    }
}
