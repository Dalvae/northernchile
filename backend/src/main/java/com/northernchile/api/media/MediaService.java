package com.northernchile.api.media;

import com.northernchile.api.media.dto.*;
import com.northernchile.api.media.mapper.MediaMapper;
import com.northernchile.api.media.model.Media;
import com.northernchile.api.media.model.ScheduleMedia;
import com.northernchile.api.media.model.TourMedia;
import com.northernchile.api.media.repository.MediaRepository;
import com.northernchile.api.media.repository.ScheduleMediaRepository;
import com.northernchile.api.media.repository.TourMediaRepository;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
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
 */
@Service
public class MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaService.class);

    private final MediaRepository mediaRepository;
    private final TourMediaRepository tourMediaRepository;
    private final ScheduleMediaRepository scheduleMediaRepository;
    private final TourRepository tourRepository;
    private final TourScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final S3StorageService s3StorageService;
    private final MediaMapper mediaMapper;

    public MediaService(MediaRepository mediaRepository,
                       TourMediaRepository tourMediaRepository,
                       ScheduleMediaRepository scheduleMediaRepository,
                       TourRepository tourRepository,
                       TourScheduleRepository scheduleRepository,
                       UserRepository userRepository,
                       S3StorageService s3StorageService,
                       MediaMapper mediaMapper) {
        this.mediaRepository = mediaRepository;
        this.tourMediaRepository = tourMediaRepository;
        this.scheduleMediaRepository = scheduleMediaRepository;
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
        boolean isSuperAdmin = requester.getRole().equals("ROLE_SUPER_ADMIN");
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
        boolean isSuperAdmin = requester.getRole().equals("ROLE_SUPER_ADMIN");
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
        boolean isSuperAdmin = requester.getRole().equals("ROLE_SUPER_ADMIN");
        boolean isOwner = media.getOwner().getId().equals(requester.getId());

        if (!isSuperAdmin && !isOwner) {
            throw new AccessDeniedException("You don't have permission to access this media");
        }
    }

    /**
     * Check if the requester is a SUPER_ADMIN.
     */
    private boolean isSuperAdmin(User user) {
        return user.getRole().equals("ROLE_SUPER_ADMIN");
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

        Tour tour = null;
        TourSchedule schedule = null;

        // Verify access if uploading for a specific tour/schedule
        // But DON'T set tour_id/schedule_id on Media - use join tables only
        if (tourId != null) {
            tour = tourRepository.findById(tourId)
                    .orElseThrow(() -> new EntityNotFoundException("Tour not found: " + tourId));

            verifyTourAccess(owner, tour);
            // DO NOT: media.setTour(tour) - relationship goes in tour_media table only
        }

        if (scheduleId != null) {
            schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + scheduleId));

            verifyScheduleAccess(owner, schedule);
            // DO NOT: media.setSchedule(schedule) - relationship goes in schedule_media table only
        }

        Media saved = mediaRepository.save(media);
        log.info("Media created with ID: {}", saved.getId());

        // Create join table entries for tour/schedule galleries
        if (tour != null) {
            // Get next display order
            Integer maxOrder = tourMediaRepository.getMaxDisplayOrder(tourId);

            // Use constructor that properly initializes the composite ID
            TourMedia tourMedia = new TourMedia(tour, saved);
            tourMedia.setDisplayOrder(maxOrder + 1);
            tourMedia.setIsHero(false);
            tourMedia.setIsFeatured(false);
            tourMediaRepository.save(tourMedia);

            log.info("Created TourMedia entry for tour: {}, media: {}", tourId, saved.getId());
        }

        if (schedule != null) {
            // Get next display order
            Integer maxOrder = scheduleMediaRepository.getMaxDisplayOrder(scheduleId);

            // Use constructor that properly initializes the composite ID
            ScheduleMedia scheduleMedia = new ScheduleMedia(schedule, saved);
            scheduleMedia.setDisplayOrder(maxOrder + 1);
            scheduleMediaRepository.save(scheduleMedia);

            log.info("Created ScheduleMedia entry for schedule: {}, media: {}", scheduleId, saved.getId());
        }

        return mediaMapper.toMediaRes(saved);
    }

    /**
     * Create new media record from existing S3 file (deprecated - use uploadAndCreateMedia instead).
     */
    @Transactional
    @Deprecated
    public MediaRes createMedia(MediaCreateReq req, UUID ownerId) {
        log.info("Creating media for owner: {}", ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + ownerId));

        Media media = mediaMapper.toMedia(req);
        media.setOwner(owner);

        if (req.getTourId() != null) {
            Tour tour = tourRepository.findById(req.getTourId())
                    .orElseThrow(() -> new EntityNotFoundException("Tour not found: " + req.getTourId()));

            verifyTourAccess(owner, tour);
            media.setTour(tour);
        }

        if (req.getScheduleId() != null) {
            TourSchedule schedule = scheduleRepository.findById(req.getScheduleId())
                    .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + req.getScheduleId()));

            verifyScheduleAccess(owner, schedule);
            media.setSchedule(schedule);
        }

        Media saved = mediaRepository.save(media);
        log.info("Media created with ID: {}", saved.getId());

        return mediaMapper.toMediaRes(saved);
    }

    /**
     * Get media by ID.
     */
    public MediaRes getMedia(UUID id, UUID requesterId) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media not found: " + id));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requesterId));

        verifyMediaAccess(requester, media);

        return mediaMapper.toMediaRes(media);
    }

    /**
     * List all media with pagination and filtering.
     */
    public Page<MediaRes> listMedia(UUID requesterId, UUID tourId, UUID scheduleId, String search, Pageable pageable) {
        log.info("Listing media for requester: {}, tour: {}, schedule: {}, search: {}", requesterId, tourId, scheduleId, search);

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requesterId));

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
        } else if (search != null && !search.isBlank()) {
            // For search: SUPER_ADMIN can search all media, PARTNER_ADMIN only their own
            if (isSuperAdmin) {
                // TODO: Need a repository method for global search - for now, search by owner
                mediaPage = mediaRepository.searchByFilename(requesterId, search, pageable);
            } else {
                mediaPage = mediaRepository.searchByFilename(requesterId, search, pageable);
            }
        } else {
            // For general listing: SUPER_ADMIN sees all, PARTNER_ADMIN sees only their own
            if (isSuperAdmin) {
                // TODO: Need a repository method to find all media - for now, filter by owner
                mediaPage = mediaRepository.findByOwnerId(requesterId, pageable);
            } else {
                mediaPage = mediaRepository.findByOwnerId(requesterId, pageable);
            }
        }

        return mediaPage.map(mediaMapper::toMediaRes);
    }

    /**
     * Update media metadata.
     */
    @Transactional
    public MediaRes updateMedia(UUID id, MediaUpdateReq req, UUID requesterId) {
        log.info("Updating media: {}", id);

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media not found: " + id));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requesterId));

        verifyMediaAccess(requester, media);

        mediaMapper.updateMediaFromReq(req, media);

        if (req.getTourId() != null) {
            Tour tour = tourRepository.findById(req.getTourId())
                    .orElseThrow(() -> new EntityNotFoundException("Tour not found: " + req.getTourId()));
            media.setTour(tour);
        } else {
            media.setTour(null);
        }

        if (req.getScheduleId() != null) {
            TourSchedule schedule = scheduleRepository.findById(req.getScheduleId())
                    .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + req.getScheduleId()));
            media.setSchedule(schedule);
        } else {
            media.setSchedule(null);
        }

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

        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media not found: " + id));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requesterId));

        verifyMediaAccess(requester, media);

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

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found: " + tourId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requesterId));

        verifyTourAccess(requester, tour);

        Integer maxOrder = tourMediaRepository.getMaxDisplayOrder(tourId);
        int currentOrder = maxOrder != null ? maxOrder : -1;

        for (UUID mediaId : mediaIds) {
            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new EntityNotFoundException("Media not found: " + mediaId));

            verifyMediaAccess(requester, media);

            TourMedia.TourMediaId id = new TourMedia.TourMediaId(tourId, mediaId);
            if (tourMediaRepository.existsById(id)) {
                log.warn("Media {} already assigned to tour {}, skipping", mediaId, tourId);
                continue;
            }

            TourMedia tourMedia = new TourMedia(tour, media);
            tourMedia.setDisplayOrder(++currentOrder);
            tourMediaRepository.save(tourMedia);
        }

        log.info("Successfully assigned media to tour: {}", tourId);
    }

    /**
     * Reorder media in a tour gallery.
     */
    @Transactional
    public void reorderTourMedia(UUID tourId, List<MediaOrderReq> orders, UUID requesterId) {
        log.info("Reordering media for tour: {}", tourId);

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found: " + tourId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requesterId));

        verifyTourAccess(requester, tour);

        // Step 1: Set all to temporary high values to avoid unique constraint conflicts
        int tempOffset = 10000;
        for (int i = 0; i < orders.size(); i++) {
            MediaOrderReq order = orders.get(i);
            TourMedia.TourMediaId id = new TourMedia.TourMediaId(tourId, order.getMediaId());
            TourMedia tourMedia = tourMediaRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("TourMedia not found"));

            tourMedia.setDisplayOrder(tempOffset + i);
            tourMediaRepository.save(tourMedia);
        }

        // Step 2: Flush changes to database
        tourMediaRepository.flush();

        // Step 3: Set final display orders
        for (MediaOrderReq order : orders) {
            TourMedia.TourMediaId id = new TourMedia.TourMediaId(tourId, order.getMediaId());
            TourMedia tourMedia = tourMediaRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("TourMedia not found"));

            tourMedia.setDisplayOrder(order.getDisplayOrder());
            tourMediaRepository.save(tourMedia);
        }

        log.info("Successfully reordered media for tour: {}", tourId);
    }

    /**
     * Set hero image for a tour.
     */
    @Transactional
    public void setHeroImage(UUID tourId, UUID mediaId, UUID requesterId) {
        log.info("Setting hero image for tour: {}, media: {}", tourId, mediaId);

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found: " + tourId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requesterId));

        verifyTourAccess(requester, tour);

        TourMedia.TourMediaId id = new TourMedia.TourMediaId(tourId, mediaId);
        TourMedia tourMedia = tourMediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media not assigned to this tour"));

        tourMediaRepository.unsetHeroImage(tourId);

        tourMedia.setIsHero(true);
        tourMediaRepository.save(tourMedia);

        log.info("Successfully set hero image for tour: {}", tourId);
    }

    /**
     * Toggle featured status for a tour image.
     * Unlike hero image, multiple images can be featured.
     */
    @Transactional
    public void toggleFeatured(UUID tourId, UUID mediaId, UUID requesterId) {
        log.info("Toggling featured status for tour: {}, media: {}", tourId, mediaId);

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found: " + tourId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requesterId));

        verifyTourAccess(requester, tour);

        TourMedia.TourMediaId id = new TourMedia.TourMediaId(tourId, mediaId);
        TourMedia tourMedia = tourMediaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Media not assigned to this tour"));

        // Toggle the featured status
        tourMedia.setIsFeatured(!tourMedia.getIsFeatured());
        tourMediaRepository.save(tourMedia);

        log.info("Successfully toggled featured status for tour: {}", tourId);
    }

    /**
     * Get all media for a tour gallery (with display order and hero flag).
     */
    public List<MediaRes> getTourGallery(UUID tourId, UUID requesterId) {
        log.info("Getting gallery for tour: {}", tourId);

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found: " + tourId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requesterId));

        verifyTourAccess(requester, tour);

        // Use fetch join to avoid LazyInitializationException
        List<TourMedia> tourMediaList = tourMediaRepository.findByTourIdWithMediaOrderByDisplayOrderAsc(tourId);

        return tourMediaList.stream()
                .map(tm -> {
                    MediaRes res = mediaMapper.toMediaRes(tm.getMedia());
                    res.setDisplayOrder(tm.getDisplayOrder());
                    res.setIsHero(tm.getIsHero());
                    res.setIsFeatured(tm.getIsFeatured());
                    return res;
                })
                .toList();
    }

    /**
     * Assign media to a schedule gallery.
     */
    @Transactional
    public void assignMediaToSchedule(UUID scheduleId, List<UUID> mediaIds, UUID requesterId) {
        log.info("Assigning {} media items to schedule: {}", mediaIds.size(), scheduleId);

        TourSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + scheduleId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requesterId));

        verifyScheduleAccess(requester, schedule);

        Integer maxOrder = scheduleMediaRepository.getMaxDisplayOrder(scheduleId);
        int currentOrder = maxOrder != null ? maxOrder : -1;

        for (UUID mediaId : mediaIds) {
            Media media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new EntityNotFoundException("Media not found: " + mediaId));

            verifyMediaAccess(requester, media);

            ScheduleMedia.ScheduleMediaId id = new ScheduleMedia.ScheduleMediaId(scheduleId, mediaId);
            if (scheduleMediaRepository.existsById(id)) {
                log.warn("Media {} already assigned to schedule {}, skipping", mediaId, scheduleId);
                continue;
            }

            ScheduleMedia scheduleMedia = new ScheduleMedia(schedule, media);
            scheduleMedia.setDisplayOrder(++currentOrder);
            scheduleMediaRepository.save(scheduleMedia);
        }

        log.info("Successfully assigned media to schedule: {}", scheduleId);
    }

    /**
     * Reorder media in a schedule gallery.
     */
    @Transactional
    public void reorderScheduleMedia(UUID scheduleId, List<MediaOrderReq> orders, UUID requesterId) {
        log.info("Reordering media for schedule: {}", scheduleId);

        TourSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + scheduleId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requesterId));

        verifyScheduleAccess(requester, schedule);

        for (MediaOrderReq order : orders) {
            ScheduleMedia.ScheduleMediaId id = new ScheduleMedia.ScheduleMediaId(scheduleId, order.getMediaId());
            ScheduleMedia scheduleMedia = scheduleMediaRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("ScheduleMedia not found"));

            scheduleMedia.setDisplayOrder(order.getDisplayOrder());
            scheduleMediaRepository.save(scheduleMedia);
        }

        log.info("Successfully reordered media for schedule: {}", scheduleId);
    }

    /**
     * Get all media for a schedule gallery (with display order).
     * Includes inherited media from parent tour + schedule-specific media.
     */
    public List<MediaRes> getScheduleGallery(UUID scheduleId, UUID requesterId) {
        log.info("Getting gallery for schedule: {}", scheduleId);

        TourSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + scheduleId));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + requesterId));

        verifyScheduleAccess(requester, schedule);

        List<MediaRes> result = new java.util.ArrayList<>();

        UUID tourId = schedule.getTour().getId();
        // Use fetch join to avoid LazyInitializationException
        List<TourMedia> tourMediaList = tourMediaRepository.findByTourIdWithMediaOrderByDisplayOrderAsc(tourId);

        for (TourMedia tm : tourMediaList) {
            MediaRes res = mediaMapper.toMediaRes(tm.getMedia());
            res.setDisplayOrder(tm.getDisplayOrder());
            res.setIsHero(tm.getIsHero());
            res.setIsInherited(true); // Mark as inherited from tour
            result.add(res);
        }

        // Use fetch join to avoid LazyInitializationException
        List<ScheduleMedia> scheduleMediaList = scheduleMediaRepository.findByScheduleIdWithMediaOrderByDisplayOrderAsc(scheduleId);

        for (ScheduleMedia sm : scheduleMediaList) {
            MediaRes res = mediaMapper.toMediaRes(sm.getMedia());
            res.setDisplayOrder(sm.getDisplayOrder());
            res.setIsInherited(false); // Mark as schedule-specific
            result.add(res);
        }

        return result;
    }
}
