package com.northernchile.api.privatetour;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PrivateTourRequestController {

    // TODO: Inyectar PrivateTourRequestService

    // Endpoint PÚBLICO para que clientes envíen una solicitud
    @PostMapping("/private-tours/requests")
    public ResponseEntity<String> submitRequest(@RequestBody Map<String, Object> requestPayload) {
        // TODO: Crear un DTO para el request
        // TODO: Llamar al servicio para guardar la solicitud y notificar al admin
        return ResponseEntity.ok("Request received. We will contact you shortly.");
    }

    // Endpoints de ADMINISTRACIÓN para gestionar las solicitudes
    @GetMapping("/admin/private-tours/requests")
    public ResponseEntity<?> getAllRequests() {
        // TODO: Devolver todas las solicitudes
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/private-tours/requests/{id}")
    public ResponseEntity<?> updateRequestStatus(@PathVariable UUID id, @RequestBody Map<String, String> statusUpdate) {
        // TODO: Lógica para actualizar el estado (ej: a 'QUOTED' o 'CONFIRMED')
        return ResponseEntity.ok().build();
    }
}
