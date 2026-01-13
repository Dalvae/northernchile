package com.northernchile.api.media;

import com.northernchile.api.media.dto.*;
import com.northernchile.api.media.mapper.MediaMapper;
import com.northernchile.api.media.model.Media;
import com.northernchile.api.media.repository.MediaRepository;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.security.Role;
import com.northernchile.api.storage.S3StorageService;
import com.northernchile.api.tour.TourRepository;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing media (photos) for tours and schedules.
 * Simplified architecture: media directly references tour/schedule via FK.
 */
@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    private final MediaRepository mediaRepository;
    private final TourRepository tourRepository;
    private final TourScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final S3StorageService s3StorageService;
    private final MediaMapper mediaMapper;

    public MediaService(MediaRepository mediaRepository,
                       TourRepository tourRepository,
                       TourScheduleRepository scheduleRepository,
                       UserRepository userRepository,
                       S3StorageService s3StorageService,
                       MediaMapper mediaMapper) {
        this.mediaRepository = mediaRepository;
        this.tourRepository = tourRepository;
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.s3StorageService = s3StorageService;
        this.mediaMapper = mediaMapper;
    }

    /**
     * Verify if the requester has access to a tour.
     * SUPER_ADMIN can access any tour, PARTNER_ADMIN only their own tours.
     */
    private void verifyTourAccess(User requester, Tour tour) {
        boolean isSuperAdmin = requester.getRole().equals(Role.SUPER_ADMIN.getRoleName());
        boolean isOwner = tour.getOwner().getId().equals(requester.getId());

        if (!isSuperAdmin && !isOwner) {
            throw new AccessDeniedException("You don't have permission to access this tour");
        }
    }

    /**
     * Verify if the requester has access to a schedule (through its parent tour).
     * SUPER_ADMIN can access any schedule, PARTNER_ADMIN only their own schedules.
     */
    private void verifyScheduleAccess(User requester, TourSchedule schedule) {
        boolean isSuperAdmin = requester.getRole().equals(Role.SUPER_ADMIN.getRoleName());
        boolean isOwner = schedule.getTour().getOwner().getId().equals(requester.getId());

        if (!isSuperAdmin && !isOwner) {
            throw new AccessDeniedException("You don't have permission to access this schedule");
        }
    }

    /**
     * Verify if the requester has access to media.
     * SUPER_ADMIN can access any media, PARTNER_ADMIN only their own media.
     */
    private void verifyMediaAccess(User requester, Media media) {
        boolean isSuperAdmin = requester.getRole().equals(Role.SUPER_ADMIN.getRoleName());
        boolean isOwner = media.getOwner().getId().equals(requester.getId());

        if (!isSuperAdmin && !isOwner) {
            throw new AccessDeniedException("You don't have permission to access this media");
        }
    }

    /**
     * Check if the requester is a SUPER_ADMIN.
     */
    private boolean isSuperAdmin(User user) {
        return user.getRole().equals(Role.SUPER_ADMIN.getRoleName());
    }

    // ============= Helper methods for DRY entity lookup + access verification =============

    /**
     * Find user by ID or throw EntityNotFoundException.
     */
    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
    }

    /**
     * Find tour by ID or throw EntityNotFoundException.
     */
    private Tour findTourOrThrow(UUID tourId) {
        return tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found: " + tourId));
    }

    /**
     * Find schedule by ID or throw EntityNotFoundException.
     */
    private TourSchedule findScheduleOrThrow(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + scheduleId));
    }

    /**
     * Find media by ID or throw EntityNotFoundException.
     */
    private Media findMediaOrThrow(UUID mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new EntityNotFoundException("Media not found: " + mediaId));
    }

    /**
     * Find tour and verify requester has access.
     */
    private Tour findTourWithAccess(UUID tourId, UUID requesterId) {
        Tour tour = findTourOrThrow(tourId);
        User requester = findUserOrThrow(requesterId);
        verifyTourAccess(requester, tour);
        return tour;
    }

    /**
     * Find schedule and verify requester has access.
     */
    private TourSchedule findScheduleWithAccess(UUID scheduleId, UUID requesterId) {
        TourSchedule schedule = findScheduleOrThrow(scheduleId);
        User requester = findUserOrThrow(requesterId);
        verifyScheduleAccess(requester, schedule);
        return schedule;
    }

    /**
     * Find media and verify requester has access.
     */
    private Media findMediaWithAccess(UUID mediaId, UUID requesterId) {
        Media media = findMediaOrThrow(mediaId);
        User requester = findUserOrThrow(requesterId);
        verifyMediaAccess(requester, media);
        return media;
    }

    /**
     * Verify that media is assigned to a specific tour.
     */
    private void verifyMediaBelongsToTour(Media media, UUID tourId) {
        if (media.getTour() == null || !media.getTour().getId().equals(tourId)) {
            throw new IllegalArgumentException("Media " + media.getId() + " is not assigned to tour " + tourId);
        }
    }

    /**
     * Verify that media is assigned to a specific schedule.
     */
    private void verifyMediaBelongsToSchedule(Media media, UUID scheduleId) {
        if (media.getSchedule() == null || !media.getSchedule().getId().equals(scheduleId)) {
            throw new IllegalArgumentException("Media " + media.getId() + " is not assigned to schedule " + scheduleId);
        }
    }

    /**
     * Upload file to S3 and create media record in one step.
     */
    @Transactional
    public MediaRes uploadAndCreateMedia(
            MultipartFile file,
            UUID tourId,
            UUID scheduleId,
            String[] tags,
            String altText,
            String caption,
            UUID ownerId) throws IOException {

        log.info("Uploading and creating media for owner: {}", ownerId);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must not exceed 10MB");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + ownerId));

        // Upload to S3
        String folder = "media";
        String s3Key = s3StorageService.uploadFile(file, folder);
        String url = s3StorageService.getPublicUrl(s3Key);

        log.info("File uploaded to S3: {}", s3Key);

        Media media = new Media();
        media.setOwner(owner);
        media.setS3Key(s3Key);
        media.setUrl(url);
        media.setSizeBytes(file.getSize());
        media.setContentType(contentType);
        media.setOriginalFilename(file.getOriginalFilename());
        media.setTags(tags);

        if (altText != null && !altText.isBlank()) {
            Map<String, String> altTranslations = new HashMap<>();
            altTranslations.put("es", altText);
            media.setAltTranslations(altTranslations);
        }

        if (caption != null && !caption.isBlank()) {
            Map<String, String> captionTranslations = new HashMap<>();
            captionTranslations.put("es", caption);
            media.setCaptionTranslations(captionTranslations);
        }

        // Set tour/schedule directly on media
        if (tourId != null) {
            Tour tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> new EntityNotFoundException("Tour not found: " + tourId));
            verifyTourAccess(owner, tour);
            media.setTour(tour);
            
            // Get next display order for this tour
            Integer maxOrder = mediaRepository.getMaxDisplayOrderByTour(tourId);
            media.setDisplayOrder(maxOrder != null ? maxOrder + 1 : 0);
        }

        if (scheduleId != null) {
            TourSchedule schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + scheduleId));
            verifyScheduleAccess(owner, schedule);
            media.setSchedule(schedule);
            
            // Get next display order for this schedule
            Integer maxOrder = mediaRepository.getMaxDisplayOrderBySchedule(scheduleId);
            media.setDisplayOrder(maxOrder != null ? maxOrder + 1 : 0);
        }

        Media saved = mediaRepository.save(media);
        log.info("Media created with ID: {}", saved.getId());

        return mediaMapper.toMediaRes(saved);
    }

    /**
     * Get media by ID.
     */
    public MediaRes getMedia(UUID id, UUID requesterId) {
        Media media = findMediaWithAccess(id, requesterId);
        return mediaMapper.toMediaRes(media);
    }

    /**
     * List all media with pagination and filtering.
     * SUPER_ADMIN sees all media, PARTNER_ADMIN sees only their own.
     */
    public Page<MediaRes> listMedia(UUID requesterId, UUID tourId, UUID scheduleId, String type, String search, Pageable pageable) {
        log.info("Listing media for requester: {}, tour: {}, schedule: {}, type: {}, search: {}", requesterId, tourId, scheduleId, type, search);

        User requester = findUserOrThrow(requesterId);

        boolean isSuperAdmin = isSuperAdmin(requester);

        Page<Media> mediaPage;

        if (tourId != null) {
            // For tour filtering: SUPER_ADMIN sees all, PARTNER_ADMIN sees only their own
            mediaPage = mediaRepository.findByTourId(tourId).stream()
                    .filter(m -> isSuperAdmin || m.getOwner().getId().equals(requesterId))
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())
                    ));
        } else if (scheduleId != null) {
            // For schedule filtering: SUPER_ADMIN sees all, PARTNER_ADMIN sees only their own
            mediaPage = mediaRepository.findByScheduleId(scheduleId).stream()
                    .filter(m -> isSuperAdmin || m.getOwner().getId().equals(requesterId))
                    .collect(java.util.stream.Collectors.collectingAndThen(
                            java.util.stream.Collectors.toList(),
                            list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())
                    ));
        } else if (type != null && !type.isBlank()) {
            // Filter by type (TOUR, SCHEDULE, LOOSE)
            mediaPage = switch (type.toUpperCase()) {
                case "TOUR" -> isSuperAdmin 
                        ? mediaRepository.findTourMedia(pageable)
                        : mediaRepository.findTourMediaByOwner(requesterId, pageable);
                case "SCHEDULE" -> isSuperAdmin
                        ? mediaRepository.findScheduleMedia(pageable)
                        : mediaRepository.findScheduleMediaByOwner(requesterId, pageable);
                case "LOOSE" -> isSuperAdmin
                        ? mediaRepository.findLooseMedia(pageable)
                        : mediaRepository.findLooseMediaByOwner(requesterId, pageable);
                default -> isSuperAdmin
                        ? mediaRepository.findAll(pageable)
                        : mediaRepository.findByOwnerId(requesterId, pageable);
            };
        } else if (search != null && !search.isBlank()) {
            mediaPage = isSuperAdmin
                    ? mediaRepository.searchByFilename(search, pageable)
                    : mediaRepository.searchByFilename(requesterId, search, pageable);
        } else {
            mediaPage = isSuperAdmin
                    ? mediaRepository.findAll(pageable)
                    : mediaRepository.findByOwnerId(requesterId, pageable);
        }

        return mediaPage.map(mediaMapper::toMediaRes);
    }

    /**
     * Update media metadata.
     */
    @Transactional
    public MediaRes updateMedia(UUID id, MediaUpdateReq req, UUID requesterId) {
        log.info("Updating media: {}", id);

        Media media = findMediaWithAccess(id, requesterId);
        mediaMapper.updateMediaFromReq(req, media);

        media.setTour(req.tourId() != null ? findTourOrThrow(req.tourId()) : null);
        media.setSchedule(req.scheduleId() != null ? findScheduleOrThrow(req.scheduleId()) : null);

        Media updated = mediaRepository.save(media);
        log.info("Media updated: {}", id);

        return mediaMapper.toMediaRes(updated);
    }

    /**
     * Delete media and remove from S3.
     */
    @Transactional
    public void deleteMedia(UUID id, UUID requesterId) {
        log.info("Deleting media: {}", id);

        Media media = findMediaWithAccess(id, requesterId);

        try {
            s3StorageService.deleteFile(media.getS3Key());
            log.info("Deleted file from S3: {}", media.getS3Key());
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", media.getS3Key(), e);
            // Continue with database deletion even if S3 deletion fails
        }

        mediaRepository.delete(media);
        log.info("Media deleted: {}", id);
    }

    /**
     * Assign media to a tour gallery.
     */
    @Transactional
    public void assignMediaToTour(UUID tourId, List<UUID> mediaIds, UUID requesterId) {
        log.info("Assigning {} media items to tour: {}", mediaIds.size(), tourId);

        Tour tour = findTourWithAccess(tourId, requesterId);

        Integer maxOrder = mediaRepository.getMaxDisplayOrderByTour(tourId);
        int currentOrder = maxOrder != null ? maxOrder : -1;

        for (UUID mediaId : mediaIds) {
            Media media = findMediaWithAccess(mediaId, requesterId);

            // Check if already assigned
            if (media.getTour() != null && media.getTour().getId().equals(tourId)) {
                log.warn("Media {} already assigned to tour {}, skipping", mediaId, tourId);
                continue;
            }

            media.setTour(tour);
            media.setSchedule(null); // Clear schedule when assigning to tour
            media.setDisplayOrder(++currentOrder);
            mediaRepository.save(media);
        }

        log.info("Successfully assigned media to tour: {}", tourId);
    }

    /**
     * Unassign media from a tour gallery (makes it LOOSE).
     */
    @Transactional
    public void unassignMediaFromTour(UUID tourId, UUID mediaId, UUID requesterId) {
        log.info("Unassigning media {} from tour: {}", mediaId, tourId);

        findTourWithAccess(tourId, requesterId);
        Media media = findMediaWithAccess(mediaId, requesterId);
        verifyMediaBelongsToTour(media, tourId);

        // Unassign by clearing tour reference
        media.setTour(null);
        media.setDisplayOrder(0);
        media.setIsHero(false);
        media.setIsFeatured(false);
        mediaRepository.save(media);

        log.info("Successfully unassigned media {} from tour: {}", mediaId, tourId);
    }

    /**
     * Reorder media in a tour gallery.
     */
    @Transactional
    public void reorderTourMedia(UUID tourId, List<MediaOrderReq> orders, UUID requesterId) {
        log.info("Reordering media for tour: {}", tourId);

        findTourWithAccess(tourId, requesterId);

        // Step 1: Set all to temporary high values to avoid unique constraint conflicts
        int tempOffset = 10000;
        for (int i = 0; i < orders.size(); i++) {
            MediaOrderReq order = orders.get(i);
            Media media = findMediaOrThrow(order.mediaId());
            verifyMediaBelongsToTour(media, tourId);

            media.setDisplayOrder(tempOffset + i);
            mediaRepository.save(media);
        }

        // Step 2: Flush changes to database
        mediaRepository.flush();

        // Step 3: Set final display orders
        for (MediaOrderReq order : orders) {
            Media media = findMediaOrThrow(order.mediaId());
            media.setDisplayOrder(order.displayOrder());
            mediaRepository.save(media);
        }

        log.info("Successfully reordered media for tour: {}", tourId);
    }

    /**
     * Set hero image for a tour.
     */
    @Transactional
    public void setHeroImage(UUID tourId, UUID mediaId, UUID requesterId) {
        log.info("Setting hero image for tour: {}, media: {}", tourId, mediaId);

        findTourWithAccess(tourId, requesterId);
        Media media = findMediaOrThrow(mediaId);
        verifyMediaBelongsToTour(media, tourId);

        // Unset current hero
        mediaRepository.unsetHeroByTour(tourId);

        // Set new hero
        media.setIsHero(true);
        mediaRepository.save(media);

        log.info("Successfully set hero image for tour: {}", tourId);
    }

    /**
     * Toggle featured status for a tour image.
     * Unlike hero image, multiple images can be featured.
     */
    @Transactional
    public void toggleFeatured(UUID tourId, UUID mediaId, UUID requesterId) {
        log.info("Toggling featured status for tour: {}, media: {}", tourId, mediaId);

        findTourWithAccess(tourId, requesterId);
        Media media = findMediaOrThrow(mediaId);
        verifyMediaBelongsToTour(media, tourId);

        // Toggle the featured status
        media.setIsFeatured(!Boolean.TRUE.equals(media.getIsFeatured()));
        mediaRepository.save(media);

        log.info("Successfully toggled featured status for tour: {}", tourId);
    }

    /**
     * Get all media for a tour gallery (with display order and hero flag).
     */
    public List<MediaRes> getTourGallery(UUID tourId, UUID requesterId) {
        log.info("Getting gallery for tour: {}", tourId);

        findTourWithAccess(tourId, requesterId);

        return mediaRepository.findByTourIdOrderByDisplayOrderAsc(tourId).stream()
                .map(mediaMapper::toMediaRes)
                .toList();
    }

    /**
     * Assign media to a schedule gallery.
     */
    @Transactional
    public void assignMediaToSchedule(UUID scheduleId, List<UUID> mediaIds, UUID requesterId) {
        log.info("Assigning {} media items to schedule: {}", mediaIds.size(), scheduleId);

        TourSchedule schedule = findScheduleWithAccess(scheduleId, requesterId);

        Integer maxOrder = mediaRepository.getMaxDisplayOrderBySchedule(scheduleId);
        int currentOrder = maxOrder != null ? maxOrder : -1;

        for (UUID mediaId : mediaIds) {
            Media media = findMediaWithAccess(mediaId, requesterId);

            // Check if already assigned
            if (media.getSchedule() != null && media.getSchedule().getId().equals(scheduleId)) {
                log.warn("Media {} already assigned to schedule {}, skipping", mediaId, scheduleId);
                continue;
            }

            media.setSchedule(schedule);
            media.setTour(null); // Clear tour when assigning to schedule
            media.setDisplayOrder(++currentOrder);
            mediaRepository.save(media);
        }

        log.info("Successfully assigned media to schedule: {}", scheduleId);
    }

    /**
     * Unassign media from a schedule gallery (makes it LOOSE).
     */
    @Transactional
    public void unassignMediaFromSchedule(UUID scheduleId, UUID mediaId, UUID requesterId) {
        log.info("Unassigning media {} from schedule: {}", mediaId, scheduleId);

        findScheduleWithAccess(scheduleId, requesterId);
        Media media = findMediaWithAccess(mediaId, requesterId);
        verifyMediaBelongsToSchedule(media, scheduleId);

        // Unassign by clearing schedule reference
        media.setSchedule(null);
        media.setDisplayOrder(0);
        mediaRepository.save(media);

        log.info("Successfully unassigned media {} from schedule: {}", mediaId, scheduleId);
    }

    /**
     * Reorder media in a schedule gallery.
     */
    @Transactional
    public void reorderScheduleMedia(UUID scheduleId, List<MediaOrderReq> orders, UUID requesterId) {
        log.info("Reordering media for schedule: {}", scheduleId);

        findScheduleWithAccess(scheduleId, requesterId);

        for (MediaOrderReq order : orders) {
            Media media = findMediaOrThrow(order.mediaId());
            verifyMediaBelongsToSchedule(media, scheduleId);

            media.setDisplayOrder(order.displayOrder());
            mediaRepository.save(media);
        }

        log.info("Successfully reordered media for schedule: {}", scheduleId);
    }

    /**
     * Get all media for a schedule gallery (with display order).
     * Includes inherited media from parent tour + schedule-specific media.
     */
    public List<MediaRes> getScheduleGallery(UUID scheduleId, UUID requesterId) {
        log.info("Getting gallery for schedule: {}", scheduleId);

        TourSchedule schedule = findScheduleWithAccess(scheduleId, requesterId);

        List<MediaRes> result = new java.util.ArrayList<>();

        // Get inherited media from parent tour
        UUID tourId = schedule.getTour().getId();
        mediaRepository.findByTourIdOrderByDisplayOrderAsc(tourId).forEach(m ->
                result.add(mediaMapper.toMediaRes(m).withIsInherited(true)));

        // Get schedule-specific media
        mediaRepository.findByScheduleIdOrderByDisplayOrderAsc(scheduleId).forEach(m ->
                result.add(mediaMapper.toMediaRes(m).withIsInherited(false)));

        return result;
    }
}
