package com.northernchile.api.tour;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.media.repository.TourMediaRepository;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.dto.TourCreateReq;
import com.northernchile.api.tour.dto.TourImageRes;
import com.northernchile.api.tour.dto.TourRes;
import com.northernchile.api.tour.dto.TourUpdateReq;
import com.northernchile.api.util.SlugGenerator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TourService {

    private final TourRepository tourRepository;
    private final AuditLogService auditLogService;
    private final SlugGenerator slugGenerator;
    private final TourMapper tourMapper;
    private final TourMediaRepository tourMediaRepository;

    public TourService(
            TourRepository tourRepository,
            AuditLogService auditLogService,
            SlugGenerator slugGenerator,
            TourMapper tourMapper,
            TourMediaRepository tourMediaRepository) {
        this.tourRepository = tourRepository;
        this.auditLogService = auditLogService;
        this.slugGenerator = slugGenerator;
        this.tourMapper = tourMapper;
        this.tourMediaRepository = tourMediaRepository;
    }

    public TourRes createTour(TourCreateReq tourCreateReq, User currentUser) {
        Tour tour = new Tour();
        tour.setOwner(currentUser);
        tour.setNameTranslations(tourCreateReq.getNameTranslations());
        tour.setDescriptionBlocksTranslations(tourCreateReq.getDescriptionBlocksTranslations());
        tour.setWindSensitive(tourCreateReq.isWindSensitive() != null && tourCreateReq.isWindSensitive());
        tour.setMoonSensitive(tourCreateReq.isMoonSensitive() != null && tourCreateReq.isMoonSensitive());
        tour.setCloudSensitive(tourCreateReq.isCloudSensitive() != null && tourCreateReq.isCloudSensitive());
        tour.setCategory(tourCreateReq.getCategory());
        tour.setPrice(tourCreateReq.getPrice());
        tour.setDefaultMaxParticipants(tourCreateReq.getDefaultMaxParticipants());
        tour.setDurationHours(tourCreateReq.getDurationHours());
        tour.setDefaultStartTime(tourCreateReq.getDefaultStartTime());
        tour.setStatus(tourCreateReq.getStatus());

        // Set structured content fields
        tour.setGuideName(tourCreateReq.getGuideName());
        if (tourCreateReq.getItineraryTranslations() != null) {
            tour.setItineraryTranslations(new HashMap<>(tourCreateReq.getItineraryTranslations()));
        }
        if (tourCreateReq.getEquipmentTranslations() != null) {
            tour.setEquipmentTranslations(new HashMap<>(tourCreateReq.getEquipmentTranslations()));
        }
        if (tourCreateReq.getAdditionalInfoTranslations() != null) {
            tour.setAdditionalInfoTranslations(new HashMap<>(tourCreateReq.getAdditionalInfoTranslations()));
        }

        String baseName = tourCreateReq.getNameTranslations().getOrDefault("es", "tour");
        tour.setSlug(generateUniqueSlug(baseName));

        Tour savedTour = tourRepository.save(tour);

        String tourName = savedTour.getDisplayName();
        Map<String, Object> newValues = Map.of(
            "id", savedTour.getId().toString(),
            "name", tourName,
            "status", savedTour.getStatus(),
            "category", savedTour.getCategory()
        );
        auditLogService.logCreate(currentUser, "TOUR", savedTour.getId(), tourName, newValues);

        return tourMapper.toTourRes(savedTour);
    }

    @Transactional(readOnly = true)
    public List<TourRes> getPublishedTours() {
        // Use EntityGraph to eagerly fetch images and owner - avoids N+1 query
        List<TourRes> tours = tourRepository.findByStatusNotDeletedWithImages("PUBLISHED").stream()
                .map(tourMapper::toTourRes)
                .collect(Collectors.toList());

        // Populate images for each tour (images already loaded from EntityGraph)
        tours.forEach(this::populateImages);

        return tours;
    }

    @Transactional(readOnly = true)
    public List<TourRes> getAllToursForAdmin() {
        // Use EntityGraph to eagerly fetch images and owner - avoids N+1 query
        List<TourRes> tours = tourRepository.findAllNotDeletedWithImages().stream()
                .map(tourMapper::toTourRes)
                .collect(Collectors.toList());

        // Populate images for each tour (images already loaded from EntityGraph)
        tours.forEach(this::populateImages);

        return tours;
    }

    @Transactional(readOnly = true)
    public List<TourRes> getToursByOwner(User owner) {
        // Use EntityGraph to eagerly fetch images and owner - avoids N+1 query
        List<TourRes> tours = tourRepository.findByOwnerIdNotDeletedWithImages(owner.getId()).stream()
                .map(tourMapper::toTourRes)
                .collect(Collectors.toList());

        // Populate images for each tour (images already loaded from EntityGraph)
        tours.forEach(this::populateImages);

        return tours;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or @tourSecurityService.isOwner(authentication, #id)")
    public TourRes getTourById(UUID id, User currentUser) {
        Tour tour = tourRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + id));

        TourRes tourRes = tourMapper.toTourRes(tour);
        populateImages(tourRes);

        return tourRes;
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or @tourSecurityService.isOwner(authentication, #id)")
    @CacheEvict(value = {"tour-detail", "tour-list"}, allEntries = true)
    public TourRes updateTour(UUID id, TourUpdateReq tourUpdateReq, User currentUser) {
        Tour tour = tourRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + id));

        String oldTourName = tour.getDisplayName();
        Map<String, Object> oldValues = Map.of(
            "name", oldTourName,
            "status", tour.getStatus(),
            "category", tour.getCategory(),
            "price", tour.getPrice().toString()
        );

        tour.setNameTranslations(tourUpdateReq.getNameTranslations());
        tour.setDescriptionBlocksTranslations(tourUpdateReq.getDescriptionBlocksTranslations());
        tour.setWindSensitive(tourUpdateReq.isWindSensitive() != null && tourUpdateReq.isWindSensitive());
        tour.setMoonSensitive(tourUpdateReq.isMoonSensitive() != null && tourUpdateReq.isMoonSensitive());
        tour.setCloudSensitive(tourUpdateReq.isCloudSensitive() != null && tourUpdateReq.isCloudSensitive());
        tour.setCategory(tourUpdateReq.getCategory());
        tour.setPrice(tourUpdateReq.getPrice());
        tour.setDefaultMaxParticipants(tourUpdateReq.getDefaultMaxParticipants());
        tour.setDurationHours(tourUpdateReq.getDurationHours());
        tour.setDefaultStartTime(tourUpdateReq.getDefaultStartTime());
        tour.setStatus(tourUpdateReq.getStatus());

        // Update structured content fields
        tour.setGuideName(tourUpdateReq.getGuideName());
        if (tourUpdateReq.getItineraryTranslations() != null) {
            tour.setItineraryTranslations(new HashMap<>(tourUpdateReq.getItineraryTranslations()));
        }
        if (tourUpdateReq.getEquipmentTranslations() != null) {
            tour.setEquipmentTranslations(new HashMap<>(tourUpdateReq.getEquipmentTranslations()));
        }
        if (tourUpdateReq.getAdditionalInfoTranslations() != null) {
            tour.setAdditionalInfoTranslations(new HashMap<>(tourUpdateReq.getAdditionalInfoTranslations()));
        }

        String newBaseName = tourUpdateReq.getNameTranslations().getOrDefault("es", "tour");
        String currentSlug = tour.getSlug();
        String expectedSlug = slugGenerator.generateSlug(newBaseName);
        if (currentSlug == null || !currentSlug.startsWith(expectedSlug)) {
            tour.setSlug(generateUniqueSlug(newBaseName, tour.getId()));
        }

        Tour updatedTour = tourRepository.save(tour);

        String newTourName = updatedTour.getDisplayName();
        Map<String, Object> newValues = Map.of(
            "name", newTourName,
            "status", updatedTour.getStatus(),
            "category", updatedTour.getCategory(),
            "price", updatedTour.getPrice().toString()
        );
        auditLogService.logUpdate(currentUser, "TOUR", updatedTour.getId(), newTourName, oldValues, newValues);

        return tourMapper.toTourRes(updatedTour);
    }

    @Transactional
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or @tourSecurityService.isOwner(authentication, #id)")
    @CacheEvict(value = {"tour-detail", "tour-list"}, allEntries = true)
    public void deleteTour(UUID id, User currentUser) {
        Tour tour = tourRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + id));

        tour.setDeletedAt(Instant.now());
        tourRepository.save(tour);

        String tourName = tour.getDisplayName();
        Map<String, Object> oldValues = Map.of(
            "name", tourName,
            "status", tour.getStatus(),
            "deletedAt", tour.getDeletedAt().toString()
        );
        auditLogService.logDelete(currentUser, "TOUR", tour.getId(), tourName, oldValues);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "tour-detail", key = "#slug")
    public TourRes getTourBySlug(String slug) {
        Tour tour = tourRepository.findBySlugPublished(slug)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with slug: " + slug));

        TourRes tourRes = tourMapper.toTourRes(tour);
        populateImages(tourRes);

        return tourRes;
    }

    private String generateUniqueSlug(String baseName) {
        return generateUniqueSlug(baseName, null);
    }

    private String generateUniqueSlug(String baseName, UUID excludeId) {
        String baseSlug = slugGenerator.generateSlug(baseName);
        String slug = baseSlug;
        int counter = 1;

        while (true) {
            final String candidateSlug = slug;
            boolean exists = tourRepository.findBySlug(candidateSlug)
                    .filter(t -> excludeId == null || !t.getId().equals(excludeId))
                    .isPresent();

            if (!exists) {
                return candidateSlug;
            }

            slug = baseSlug + "-" + counter;
            counter++;
        }
    }

    /**
     * Populate images from tour_media join table into TourRes
     */
    private void populateImages(TourRes tourRes) {
        if (tourRes == null || tourRes.getId() == null) {
            return;
        }

        List<TourImageRes> images = tourMediaRepository
                .findByTourIdWithMediaOrderByDisplayOrderAsc(tourRes.getId())
                .stream()
                .map(tourMapper::toTourImageRes)
                .collect(Collectors.toList());

        tourRes.setImages(images);
    }
}
