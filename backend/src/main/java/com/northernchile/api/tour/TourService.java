package com.northernchile.api.tour;

import com.northernchile.api.audit.AuditLogService;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourImage;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.dto.TourCreateReq;
import com.northernchile.api.tour.dto.TourRes;
import com.northernchile.api.tour.dto.TourUpdateReq;
import com.northernchile.api.tour.dto.TourImageRes;
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
    private final TourImageRepository tourImageRepository;
    private final AuditLogService auditLogService;
    private final SlugGenerator slugGenerator;
    private final TourMapper tourMapper;

    public TourService(
            TourRepository tourRepository,
            TourImageRepository tourImageRepository,
            AuditLogService auditLogService,
            SlugGenerator slugGenerator,
            TourMapper tourMapper) {
        this.tourRepository = tourRepository;
        this.tourImageRepository = tourImageRepository;
        this.auditLogService = auditLogService;
        this.slugGenerator = slugGenerator;
        this.tourMapper = tourMapper;
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

        if (tourCreateReq.getImageUrls() != null && !tourCreateReq.getImageUrls().isEmpty()) {
            List<TourImage> tourImages = new ArrayList<>();
            for (int i = 0; i < tourCreateReq.getImageUrls().size(); i++) {
                String imageUrl = tourCreateReq.getImageUrls().get(i);
                TourImage tourImage = new TourImage();
                tourImage.setTour(savedTour);
                tourImage.setImageUrl(imageUrl);
                tourImage.setDisplayOrder(i);
                tourImage.setHeroImage(i == 0);
                tourImages.add(tourImage);
            }
            tourImageRepository.saveAll(tourImages);
            savedTour.setImages(tourImages);
        }

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
        return tourRepository.findByStatusNotDeleted("PUBLISHED").stream()
                .map(tourMapper::toTourRes)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TourRes> getAllToursForAdmin() {
        return tourRepository.findAllNotDeleted().stream()
                .map(tourMapper::toTourRes)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TourRes> getToursByOwner(User owner) {
        return tourRepository.findByOwnerIdNotDeleted(owner.getId()).stream()
                .map(tourMapper::toTourRes)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN') or @tourSecurityService.isOwner(authentication, #id)")
    public TourRes getTourById(UUID id, User currentUser) {
        Tour tour = tourRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + id));

        return tourMapper.toTourRes(tour);
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

        tourImageRepository.deleteByTourId(tour.getId());
        if (tourUpdateReq.getImageUrls() != null && !tourUpdateReq.getImageUrls().isEmpty()) {
            List<TourImage> tourImages = new ArrayList<>();
            for (int i = 0; i < tourUpdateReq.getImageUrls().size(); i++) {
                String imageUrl = tourUpdateReq.getImageUrls().get(i);
                TourImage tourImage = new TourImage();
                tourImage.setTour(tour);
                tourImage.setImageUrl(imageUrl);
                tourImage.setDisplayOrder(i);
                tourImage.setHeroImage(i == 0);
                tourImages.add(tourImage);
            }
            tourImageRepository.saveAll(tourImages);
            tour.setImages(tourImages);
        } else {
            tour.setImages(Collections.emptyList());
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

        return tourMapper.toTourRes(tour);
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
}
