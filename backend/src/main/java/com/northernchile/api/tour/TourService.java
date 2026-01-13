package com.northernchile.api.tour;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.media.repository.MediaRepository;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.User;
import com.northernchile.api.security.Role;
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
    private final MediaRepository mediaRepository;

    public TourService(
            TourRepository tourRepository,
            AuditLogService auditLogService,
            SlugGenerator slugGenerator,
            TourMapper tourMapper,
            MediaRepository mediaRepository) {
        this.tourRepository = tourRepository;
        this.auditLogService = auditLogService;
        this.slugGenerator = slugGenerator;
        this.tourMapper = tourMapper;
        this.mediaRepository = mediaRepository;
    }

    public TourRes createTour(TourCreateReq tourCreateReq, User currentUser) {
        Tour tour = new Tour();
        tour.setOwner(currentUser);
        tour.setNameTranslations(tourCreateReq.nameTranslations());
        tour.setDescriptionBlocksTranslations(tourCreateReq.descriptionBlocksTranslations());
        tour.setWindSensitive(tourCreateReq.isWindSensitive() != null && tourCreateReq.isWindSensitive());
        tour.setMoonSensitive(tourCreateReq.isMoonSensitive() != null && tourCreateReq.isMoonSensitive());
        tour.setCloudSensitive(tourCreateReq.isCloudSensitive() != null && tourCreateReq.isCloudSensitive());
        tour.setCategory(tourCreateReq.category());
        tour.setPrice(tourCreateReq.price());
        tour.setDefaultMaxParticipants(tourCreateReq.defaultMaxParticipants());
        tour.setDurationHours(tourCreateReq.durationHours());
        tour.setDefaultStartTime(tourCreateReq.defaultStartTime());
        tour.setStatus(tourCreateReq.status());

        // Set structured content fields
        tour.setGuideName(tourCreateReq.guideName());
        if (tourCreateReq.itineraryTranslations() != null) {
            tour.setItineraryTranslations(new HashMap<>(tourCreateReq.itineraryTranslations()));
        }
        if (tourCreateReq.equipmentTranslations() != null) {
            tour.setEquipmentTranslations(new HashMap<>(tourCreateReq.equipmentTranslations()));
        }
        if (tourCreateReq.additionalInfoTranslations() != null) {
            tour.setAdditionalInfoTranslations(new HashMap<>(tourCreateReq.additionalInfoTranslations()));
        }

        String baseName = tourCreateReq.nameTranslations().getOrDefault("es", "tour");
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
    @Cacheable(value = "tour-list", key = "'all-published'")
    public List<TourRes> getPublishedTours() {
        // Use EntityGraph to eagerly fetch images and owner - avoids N+1 query
        List<TourRes> tours = tourRepository.findByStatusNotDeletedWithImages("PUBLISHED").stream()
                .map(tourMapper::toTourRes)
                .map(this::populateImages)
                .collect(Collectors.toList());

        return tours;
    }

    @Transactional(readOnly = true)
    public List<TourRes> getAllToursForAdmin() {
        // Use EntityGraph to eagerly fetch images and owner - avoids N+1 query
        List<TourRes> tours = tourRepository.findAllNotDeletedWithImages().stream()
                .map(tourMapper::toTourRes)
                .map(this::populateImages)
                .collect(Collectors.toList());

        return tours;
    }

    @Transactional(readOnly = true)
    public List<TourRes> getToursByOwner(User owner) {
        // Use EntityGraph to eagerly fetch images and owner - avoids N+1 query
        List<TourRes> tours = tourRepository.findByOwnerIdNotDeletedWithImages(owner.getId()).stream()
                .map(tourMapper::toTourRes)
                .map(this::populateImages)
                .collect(Collectors.toList());

        return tours;
    }

    @Transactional(readOnly = true)
    public TourRes getTourById(UUID id, User currentUser) {
        Tour tour = tourRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + id));

        TourRes tourRes = tourMapper.toTourRes(tour);
        return populateImages(tourRes);
    }

    @Transactional
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

        tour.setNameTranslations(tourUpdateReq.nameTranslations());
        tour.setDescriptionBlocksTranslations(tourUpdateReq.descriptionBlocksTranslations());
        tour.setWindSensitive(tourUpdateReq.isWindSensitive() != null && tourUpdateReq.isWindSensitive());
        tour.setMoonSensitive(tourUpdateReq.isMoonSensitive() != null && tourUpdateReq.isMoonSensitive());
        tour.setCloudSensitive(tourUpdateReq.isCloudSensitive() != null && tourUpdateReq.isCloudSensitive());
        tour.setCategory(tourUpdateReq.category());
        tour.setPrice(tourUpdateReq.price());
        tour.setDefaultMaxParticipants(tourUpdateReq.defaultMaxParticipants());
        tour.setDurationHours(tourUpdateReq.durationHours());
        tour.setDefaultStartTime(tourUpdateReq.defaultStartTime());
        tour.setStatus(tourUpdateReq.status());

        // Update structured content fields
        tour.setGuideName(tourUpdateReq.guideName());
        if (tourUpdateReq.itineraryTranslations() != null) {
            tour.setItineraryTranslations(new HashMap<>(tourUpdateReq.itineraryTranslations()));
        }
        if (tourUpdateReq.equipmentTranslations() != null) {
            tour.setEquipmentTranslations(new HashMap<>(tourUpdateReq.equipmentTranslations()));
        }
        if (tourUpdateReq.additionalInfoTranslations() != null) {
            tour.setAdditionalInfoTranslations(new HashMap<>(tourUpdateReq.additionalInfoTranslations()));
        }

        String newBaseName = tourUpdateReq.nameTranslations().getOrDefault("es", "tour");
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
        return populateImages(tourRes);
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
     * Populate images from media table into TourRes
     * Returns a new TourRes with images populated (records are immutable)
     */
    private TourRes populateImages(TourRes tourRes) {
        if (tourRes == null || tourRes.id() == null) {
            return tourRes;
        }

        List<TourImageRes> images = mediaRepository
                .findByTourIdOrderByDisplayOrderAsc(tourRes.id())
                .stream()
                .map(tourMapper::toTourImageRes)
                .collect(Collectors.toList());

        return new TourRes(
                tourRes.id(),
                tourRes.slug(),
                tourRes.nameTranslations(),
                tourRes.category(),
                tourRes.price(),
                tourRes.defaultMaxParticipants(),
                tourRes.durationHours(),
                tourRes.defaultStartTime(),
                tourRes.status(),
                images,
                tourRes.isMoonSensitive(),
                tourRes.isWindSensitive(),
                tourRes.isCloudSensitive(),
                tourRes.createdAt(),
                tourRes.updatedAt(),
                tourRes.contentKey(),
                tourRes.guideName(),
                tourRes.itinerary(),
                tourRes.equipment(),
                tourRes.additionalInfo(),
                tourRes.itineraryTranslations(),
                tourRes.equipmentTranslations(),
                tourRes.additionalInfoTranslations(),
                tourRes.descriptionBlocksTranslations(),
                tourRes.ownerId(),
                tourRes.ownerName(),
                tourRes.ownerEmail()
        );
    }
}
