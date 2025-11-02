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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TourService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourImageRepository tourImageRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private SlugGenerator slugGenerator;

    public TourRes createTour(TourCreateReq tourCreateReq, User currentUser) {
        Tour tour = new Tour();
        tour.setOwner(currentUser);
        tour.setNameTranslations(tourCreateReq.getNameTranslations());
        tour.setDescriptionTranslations(tourCreateReq.getDescriptionTranslations());
        tour.setWindSensitive(tourCreateReq.isWindSensitive() != null && tourCreateReq.isWindSensitive());
        tour.setMoonSensitive(tourCreateReq.isMoonSensitive() != null && tourCreateReq.isMoonSensitive());
        tour.setCloudSensitive(tourCreateReq.isCloudSensitive() != null && tourCreateReq.isCloudSensitive());
        tour.setCategory(tourCreateReq.getCategory());
        tour.setPrice(tourCreateReq.getPrice());
        tour.setDefaultMaxParticipants(tourCreateReq.getDefaultMaxParticipants());
        tour.setDurationHours(tourCreateReq.getDurationHours());
        tour.setStatus(tourCreateReq.getStatus());

        // Generate slug from Spanish name (default language)
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
                tourImage.setHeroImage(i == 0); // First image as hero image
                tourImages.add(tourImage);
            }
            tourImageRepository.saveAll(tourImages);
            savedTour.setImages(tourImages);
        }

        // Audit log
        String tourName = savedTour.getNameTranslations().getOrDefault("es", "Tour sin nombre");
        Map<String, Object> newValues = Map.of(
            "id", savedTour.getId().toString(),
            "name", tourName,
            "status", savedTour.getStatus(),
            "category", savedTour.getCategory()
        );
        auditLogService.logCreate(currentUser, "TOUR", savedTour.getId(), tourName, newValues);

        return toTourResponse(savedTour);
    }

    // ESTE MÉTODO ES PARA LA PÁGINA PÚBLICA. ¡DEBE FILTRAR!
    @Transactional(readOnly = true)
    public List<TourRes> getPublishedTours() {
        return tourRepository.findByStatusNotDeleted("PUBLISHED").stream()
                .map(this::toTourResponse)
                .collect(Collectors.toList());
    }

    // ESTE MÉTODO ES PARA EL PANEL DE ADMINISTRADOR
    // SUPER_ADMIN ve todos los tours, PARTNER_ADMIN solo ve los suyos
    @Transactional(readOnly = true)
    public List<TourRes> getAllTours(User currentUser) {
        if ("ROLE_SUPER_ADMIN".equals(currentUser.getRole())) {
            // Super admin sees all non-deleted tours
            return tourRepository.findAllNotDeleted().stream()
                    .map(this::toTourResponse)
                    .collect(Collectors.toList());
        } else {
            // Partner admin only sees their own tours
            return tourRepository.findByOwnerIdNotDeleted(currentUser.getId()).stream()
                    .map(this::toTourResponse)
                    .collect(Collectors.toList());
        }
    }

    @Transactional(readOnly = true)
    public TourRes getTourById(UUID id, User currentUser) {
        Tour tour = tourRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + id));

        // Check ownership for non-super-admins
        if (!"ROLE_SUPER_ADMIN".equals(currentUser.getRole()) &&
            !tour.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view this tour.");
        }

        return toTourResponse(tour);
    }

    @Transactional
    public TourRes updateTour(UUID id, TourUpdateReq tourUpdateReq, User currentUser) {
        Tour tour = tourRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + id));

        // Check ownership for non-super-admins
        if (!"ROLE_SUPER_ADMIN".equals(currentUser.getRole()) &&
            !tour.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to edit this tour.");
        }

        // Capture old values for audit
        String oldTourName = tour.getNameTranslations().getOrDefault("es", "Tour sin nombre");
        Map<String, Object> oldValues = Map.of(
            "name", oldTourName,
            "status", tour.getStatus(),
            "category", tour.getCategory(),
            "price", tour.getPrice().toString()
        );

        // Update tour
        tour.setNameTranslations(tourUpdateReq.getNameTranslations());
        tour.setDescriptionTranslations(tourUpdateReq.getDescriptionTranslations());
        tour.setWindSensitive(tourUpdateReq.isWindSensitive() != null && tourUpdateReq.isWindSensitive());
        tour.setMoonSensitive(tourUpdateReq.isMoonSensitive() != null && tourUpdateReq.isMoonSensitive());
        tour.setCloudSensitive(tourUpdateReq.isCloudSensitive() != null && tourUpdateReq.isCloudSensitive());
        tour.setCategory(tourUpdateReq.getCategory());
        tour.setPrice(tourUpdateReq.getPrice());
        tour.setDefaultMaxParticipants(tourUpdateReq.getDefaultMaxParticipants());
        tour.setDurationHours(tourUpdateReq.getDurationHours());
        tour.setStatus(tourUpdateReq.getStatus());

        // Regenerate slug if name changed
        String newBaseName = tourUpdateReq.getNameTranslations().getOrDefault("es", "tour");
        String currentSlug = tour.getSlug();
        String expectedSlug = slugGenerator.generateSlug(newBaseName);
        if (currentSlug == null || !currentSlug.startsWith(expectedSlug)) {
            tour.setSlug(generateUniqueSlug(newBaseName, tour.getId()));
        }

        // Handle images
        tourImageRepository.deleteByTourId(tour.getId());
        if (tourUpdateReq.getImageUrls() != null && !tourUpdateReq.getImageUrls().isEmpty()) {
            List<TourImage> tourImages = new ArrayList<>();
            for (int i = 0; i < tourUpdateReq.getImageUrls().size(); i++) {
                String imageUrl = tourUpdateReq.getImageUrls().get(i);
                TourImage tourImage = new TourImage();
                tourImage.setTour(tour);
                tourImage.setImageUrl(imageUrl);
                tourImage.setDisplayOrder(i);
                tourImage.setHeroImage(i == 0); // First image as hero image
                tourImages.add(tourImage);
            }
            tourImageRepository.saveAll(tourImages);
            tour.setImages(tourImages);
        } else {
            tour.setImages(Collections.emptyList());
        }

        Tour updatedTour = tourRepository.save(tour);

        // Audit log
        String newTourName = updatedTour.getNameTranslations().getOrDefault("es", "Tour sin nombre");
        Map<String, Object> newValues = Map.of(
            "name", newTourName,
            "status", updatedTour.getStatus(),
            "category", updatedTour.getCategory(),
            "price", updatedTour.getPrice().toString()
        );
        auditLogService.logUpdate(currentUser, "TOUR", updatedTour.getId(), newTourName, oldValues, newValues);

        return toTourResponse(updatedTour);
    }

    // Soft delete with audit logging
    @Transactional
    public void deleteTour(UUID id, User currentUser) {
        Tour tour = tourRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + id));

        // Check ownership for non-super-admins
        if (!"ROLE_SUPER_ADMIN".equals(currentUser.getRole()) &&
            !tour.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this tour.");
        }

        // Soft delete
        tour.setDeletedAt(Instant.now());
        tourRepository.save(tour);

        // Audit log
        String tourName = tour.getNameTranslations().getOrDefault("es", "Tour sin nombre");
        Map<String, Object> oldValues = Map.of(
            "name", tourName,
            "status", tour.getStatus(),
            "deletedAt", tour.getDeletedAt().toString()
        );
        auditLogService.logDelete(currentUser, "TOUR", tour.getId(), tourName, oldValues);
    }

    private TourRes toTourResponse(Tour tour) {
        TourRes tourRes = new TourRes();
        tourRes.setId(tour.getId());
        tourRes.setSlug(tour.getSlug());
        tourRes.setNameTranslations(tour.getNameTranslations());
        tourRes.setDescriptionTranslations(tour.getDescriptionTranslations());
        tourRes.setImages(tour.getImages().stream().map(this::toTourImageRes).collect(Collectors.toList()));
        tourRes.setMoonSensitive(tour.isMoonSensitive());
        tourRes.setWindSensitive(tour.isWindSensitive());
        tourRes.setCloudSensitive(tour.isCloudSensitive());
        tourRes.setCategory(tour.getCategory());
        tourRes.setPrice(tour.getPrice());
        tourRes.setDefaultMaxParticipants(tour.getDefaultMaxParticipants());
        tourRes.setDurationHours(tour.getDurationHours());
        tourRes.setStatus(tour.getStatus());
        tourRes.setCreatedAt(tour.getCreatedAt());
        tourRes.setUpdatedAt(tour.getUpdatedAt());
        return tourRes;
    }

    private TourImageRes toTourImageRes(TourImage tourImage) {
        TourImageRes tourImageRes = new TourImageRes();
        tourImageRes.setId(tourImage.getId());
        tourImageRes.setImageUrl(tourImage.getImageUrl());
        tourImageRes.setAltTextTranslations(tourImage.getAltTextTranslations());
        tourImageRes.setHeroImage(tourImage.isHeroImage());
        tourImageRes.setDisplayOrder(tourImage.getDisplayOrder());
        return tourImageRes;
    }

    /**
     * Get a published tour by its slug (for public tour pages)
     */
    @Transactional(readOnly = true)
    public TourRes getTourBySlug(String slug) {
        Tour tour = tourRepository.findBySlugPublished(slug)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with slug: " + slug));

        return toTourResponse(tour);
    }

    /**
     * Generate a unique slug from a base name
     */
    private String generateUniqueSlug(String baseName) {
        return generateUniqueSlug(baseName, null);
    }

    /**
     * Generate a unique slug from a base name, optionally excluding a specific tour ID
     */
    private String generateUniqueSlug(String baseName, UUID excludeId) {
        String baseSlug = slugGenerator.generateSlug(baseName);
        String slug = baseSlug;
        int counter = 1;

        // Keep trying with incrementing suffix until we find a unique slug
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
