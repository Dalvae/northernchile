package com.northernchile.api.privatetour;

import com.northernchile.api.model.PrivateTourRequest;
import com.northernchile.api.security.Permission;
import com.northernchile.api.security.annotations.RequiresPermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<String> submitRequest(@jakarta.validation.Valid @RequestBody PrivateTourRequest request) {
        privateTourRequestService.createRequest(request);
        return ResponseEntity.ok("Request received. We will contact you shortly.");
    }

    // Endpoints de ADMINISTRACIÓN para gestionar las solicitudes
    /**
     * @deprecated Use paginated endpoint /admin/private-tours/requests/paged instead
     */
    @GetMapping("/admin/private-tours/requests")
    @RequiresPermission(Permission.VIEW_PRIVATE_TOUR_REQUESTS)
    public ResponseEntity<List<PrivateTourRequest>> getAllRequests() {
        return ResponseEntity.ok(privateTourRequestRepository.findAll());
    }

    /**
     * Get paginated private tour requests.
     * Supports pagination via ?page=0&size=20&sort=createdAt,desc
     */
    @GetMapping("/admin/private-tours/requests/paged")
    @RequiresPermission(Permission.VIEW_PRIVATE_TOUR_REQUESTS)
    public ResponseEntity<Page<PrivateTourRequest>> getAllRequestsPaged(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(privateTourRequestRepository.findAllByOrderByCreatedAtDesc(pageable));
    }

    @PutMapping("/admin/private-tours/requests/{id}")
    @RequiresPermission(Permission.MANAGE_PRIVATE_TOUR_REQUESTS)
    public ResponseEntity<PrivateTourRequest> updateRequestStatus(@PathVariable UUID id, @RequestBody Map<String, String> statusUpdate) {
        return privateTourRequestRepository.findById(id).map(request -> {
            request.setStatus(statusUpdate.get("status"));
            return ResponseEntity.ok(privateTourRequestRepository.save(request));
        }).orElse(ResponseEntity.notFound().build());
    }
}
