package com.northernchile.api.media;

import com.northernchile.api.config.security.annotation.CurrentUser;
import com.northernchile.api.media.dto.*;
import com.northernchile.api.model.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Controller for media management in admin panel.
 */
@RestController
@RequestMapping("/api/admin/media")
@PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PARTNER_ADMIN')")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    /**
     * Upload file and create media record.
     * POST /api/admin/media
     */
    @PostMapping
    public ResponseEntity<MediaRes> createMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tourId", required = false) UUID tourId,
            @RequestParam(value = "scheduleId", required = false) UUID scheduleId,
            @RequestParam(value = "tags", required = false) String[] tags,
            @RequestParam(value = "altText", required = false) String altText,
            @RequestParam(value = "caption", required = false) String caption,
            @CurrentUser User currentUser) throws IOException {

        MediaRes created = mediaService.uploadAndCreateMedia(
                file,
                tourId,
                scheduleId,
                tags,
                altText,
                caption,
                currentUser.getId()
        );
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Get media by ID.
     * GET /api/admin/media/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<MediaRes> getMedia(@PathVariable UUID id,
                                             @CurrentUser User currentUser) {
        MediaRes media = mediaService.getMedia(id, currentUser.getId());
        return ResponseEntity.ok(media);
    }

    /**
     * List all media with filtering and pagination.
     * GET /api/admin/media
     */
    @GetMapping
    public ResponseEntity<Page<MediaRes>> listMedia(
            @RequestParam(required = false) UUID tourId,
            @RequestParam(required = false) UUID scheduleId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            Pageable pageable,
            @CurrentUser User currentUser) {

        Page<MediaRes> media = mediaService.listMedia(
                currentUser.getId(),
                tourId,
                scheduleId,
                type,
                search,
                pageable
        );

        return ResponseEntity.ok(media);
    }

    /**
     * Update media metadata.
     * PATCH /api/admin/media/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<MediaRes> updateMedia(@PathVariable UUID id,
                                               @Valid @RequestBody MediaUpdateReq req,
                                               @CurrentUser User currentUser) {
        MediaRes updated = mediaService.updateMedia(id, req, currentUser.getId());
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete media.
     * DELETE /api/admin/media/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable UUID id,
                                           @CurrentUser User currentUser) {
        mediaService.deleteMedia(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Assign media to a tour gallery.
     * POST /api/admin/media/tour/{tourId}/assign
     */
    @PostMapping("/tour/{tourId}/assign")
    public ResponseEntity<Void> assignMediaToTour(@PathVariable UUID tourId,
                                                  @Valid @RequestBody BulkAssignMediaReq req,
                                                  @CurrentUser User currentUser) {
        mediaService.assignMediaToTour(tourId, req.getMediaIds(), currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Reorder media in a tour gallery.
     * PUT /api/admin/media/tour/{tourId}/reorder
     */
    @PutMapping("/tour/{tourId}/reorder")
    public ResponseEntity<Void> reorderTourMedia(@PathVariable UUID tourId,
                                                @Valid @RequestBody List<MediaOrderReq> orders,
                                                @CurrentUser User currentUser) {
        mediaService.reorderTourMedia(tourId, orders, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Set hero image for a tour.
     * PUT /api/admin/media/tour/{tourId}/hero/{mediaId}
     */
    @PutMapping("/tour/{tourId}/hero/{mediaId}")
    public ResponseEntity<Void> setHeroImage(@PathVariable UUID tourId,
                                            @PathVariable UUID mediaId,
                                            @CurrentUser User currentUser) {
        mediaService.setHeroImage(tourId, mediaId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Toggle featured status for a tour image.
     * PUT /api/admin/media/tour/{tourId}/featured/{mediaId}
     */
    @PutMapping("/tour/{tourId}/featured/{mediaId}")
    public ResponseEntity<Void> toggleFeatured(@PathVariable UUID tourId,
                                               @PathVariable UUID mediaId,
                                               @CurrentUser User currentUser) {
        mediaService.toggleFeatured(tourId, mediaId, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Get tour gallery (all media assigned to a tour).
     * GET /api/admin/media/tour/{tourId}/gallery
     */
    @GetMapping("/tour/{tourId}/gallery")
    public ResponseEntity<List<MediaRes>> getTourGallery(@PathVariable UUID tourId,
                                                         @CurrentUser User currentUser) {
        List<MediaRes> gallery = mediaService.getTourGallery(tourId, currentUser.getId());
        return ResponseEntity.ok(gallery);
    }

    /**
     * Assign media to a schedule gallery.
     * POST /api/admin/media/schedule/{scheduleId}/assign
     */
    @PostMapping("/schedule/{scheduleId}/assign")
    public ResponseEntity<Void> assignMediaToSchedule(@PathVariable UUID scheduleId,
                                                      @Valid @RequestBody BulkAssignMediaReq req,
                                                      @CurrentUser User currentUser) {
        mediaService.assignMediaToSchedule(scheduleId, req.getMediaIds(), currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Reorder media in a schedule gallery.
     * PUT /api/admin/media/schedule/{scheduleId}/reorder
     */
    @PutMapping("/schedule/{scheduleId}/reorder")
    public ResponseEntity<Void> reorderScheduleMedia(@PathVariable UUID scheduleId,
                                                     @Valid @RequestBody List<MediaOrderReq> orders,
                                                     @CurrentUser User currentUser) {
        mediaService.reorderScheduleMedia(scheduleId, orders, currentUser.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * Get schedule gallery (all media assigned to a schedule).
     * GET /api/admin/media/schedule/{scheduleId}/gallery
     */
    @GetMapping("/schedule/{scheduleId}/gallery")
    public ResponseEntity<List<MediaRes>> getScheduleGallery(@PathVariable UUID scheduleId,
                                                             @CurrentUser User currentUser) {
        List<MediaRes> gallery = mediaService.getScheduleGallery(scheduleId, currentUser.getId());
        return ResponseEntity.ok(gallery);
    }
}
