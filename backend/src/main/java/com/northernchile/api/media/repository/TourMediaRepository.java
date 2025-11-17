package com.northernchile.api.media.repository;

import com.northernchile.api.media.model.TourMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for TourMedia join table.
 */
@Repository
public interface TourMediaRepository extends JpaRepository<TourMedia, TourMedia.TourMediaId> {

    /**
     * Find all media for a tour ordered by display_order
     */
    List<TourMedia> findByTourIdOrderByDisplayOrderAsc(UUID tourId);

    /**
     * Find hero image for a tour
     */
    Optional<TourMedia> findByTourIdAndIsHeroTrue(UUID tourId);

    /**
     * Delete all media assignments for a tour
     */
    void deleteByTourId(UUID tourId);

    /**
     * Delete a specific media assignment
     */
    void deleteByTourIdAndMediaId(UUID tourId, UUID mediaId);

    /**
     * Unset current hero image for a tour
     */
    @Modifying
    @Query("UPDATE TourMedia tm SET tm.isHero = false WHERE tm.tour.id = :tourId")
    void unsetHeroImage(@Param("tourId") UUID tourId);

    /**
     * Get max display order for a tour
     */
    @Query("SELECT COALESCE(MAX(tm.displayOrder), -1) FROM TourMedia tm WHERE tm.tour.id = :tourId")
    Integer getMaxDisplayOrder(@Param("tourId") UUID tourId);
}
