package com.northernchile.api.privatetour;

import com.northernchile.api.model.PrivateTourRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PrivateTourRequestController {

    private final PrivateTourRequestService privateTourRequestService;
    private final PrivateTourRequestRepository privateTourRequestRepository;

    public PrivateTourRequestController(
            PrivateTourRequestService privateTourRequestService,
            PrivateTourRequestRepository privateTourRequestRepository) {
        this.privateTourRequestService = privateTourRequestService;
        this.privateTourRequestRepository = privateTourRequestRepository;
    }

    // Endpoint PÚBLICO para que clientes envíen una solicitud
    @PostMapping("/private-tours/requests")
    public ResponseEntity<String> submitRequest(@RequestBody PrivateTourRequest request) {
        // In a real app, you'd use a DTO here
        privateTourRequestService.createRequest(request);
        return ResponseEntity.ok("Request received. We will contact you shortly.");
    }

    // Endpoints de ADMINISTRACIÓN para gestionar las solicitudes
    @GetMapping("/admin/private-tours/requests")
    public ResponseEntity<List<PrivateTourRequest>> getAllRequests() {
        return ResponseEntity.ok(privateTourRequestRepository.findAll());
    }

    @PutMapping("/admin/private-tours/requests/{id}")
    public ResponseEntity<PrivateTourRequest> updateRequestStatus(@PathVariable UUID id, @RequestBody Map<String, String> statusUpdate) {
        return privateTourRequestRepository.findById(id).map(request -> {
            request.setStatus(statusUpdate.get("status"));
            // In a real app, you might also set quotedPrice, paymentLinkId, etc.
            return ResponseEntity.ok(privateTourRequestRepository.save(request));
        }).orElse(ResponseEntity.notFound().build());
    }
}
