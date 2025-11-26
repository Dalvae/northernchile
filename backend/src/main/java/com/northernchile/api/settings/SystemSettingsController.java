package com.northernchile.api.settings;

import com.northernchile.api.settings.dto.SystemSettingsUpdateReq;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SystemSettingsController {

    private final SystemSettingsService settingsService;

    public SystemSettingsController(SystemSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /**
     * GET /api/admin/settings
     * Obtiene las configuraciones del sistema
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        return ResponseEntity.ok(settingsService.getCurrentSettings());
    }

    /**
     * PUT /api/admin/settings
     * Actualiza las configuraciones del sistema
     *
     * NOTA: Los cambios se almacenan en memoria.
     * En producción, esto debería persistir en base de datos.
     */
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateSettings(@Valid @RequestBody SystemSettingsUpdateReq settings) {
        Map<String, Object> updatedSettings = settingsService.updateSettings(settings);
        return ResponseEntity.ok(updatedSettings);
    }
}
