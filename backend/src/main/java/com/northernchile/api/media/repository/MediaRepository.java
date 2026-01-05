package com.northernchile.api.media.repository;

import com.northernchile.api.media.model.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Media entity.
 */
@Repository
public interface MediaRepository extends JpaRepository<Media, UUID> {

    /**
     * Find media by S3 key
     */
    Optional<Media> findByS3Key(String s3Key);

    /**
     * Find all media by owner
     */
    Page<Media> findByOwnerId(UUID ownerId, Pageable pageable);

    /**
     * Find all media by tour
     */
    List<Media> findByTourId(UUID tourId);

    /**
     * Find all media by schedule
     */
    List<Media> findByScheduleId(UUID scheduleId);

    /**
     * Find loose media (not assigned to tour or schedule)
     */
    @Query("SELECT m FROM Media m WHERE m.tour IS NULL AND m.schedule IS NULL AND m.owner.id = :ownerId")
    Page<Media> findLooseMediaByOwner(@Param("ownerId") UUID ownerId, Pageable pageable);

    /**
     * Find media assigned to any tour (tour IS NOT NULL)
     */
    @Query("SELECT m FROM Media m WHERE m.tour IS NOT NULL AND m.schedule IS NULL AND m.owner.id = :ownerId")
    Page<Media> findTourMediaByOwner(@Param("ownerId") UUID ownerId, Pageable pageable);

    /**
     * Find media assigned to any schedule (schedule IS NOT NULL)
     */
    @Query("SELECT m FROM Media m WHERE m.schedule IS NOT NULL AND m.owner.id = :ownerId")
    Page<Media> findScheduleMediaByOwner(@Param("ownerId") UUID ownerId, Pageable pageable);

    /**
     * Search media by tags (contains any of the given tags)
     */
    @Query(value = "SELECT * FROM media WHERE owner_id = :ownerId AND tags && CAST(:tags AS text[])",
           nativeQuery = true)
    Page<Media> searchByTags(@Param("ownerId") UUID ownerId,
                             @Param("tags") String[] tags,
                             Pageable pageable);

    /**
     * Search media by filename
     */
    @Query("SELECT m FROM Media m WHERE m.owner.id = :ownerId AND LOWER(m.originalFilename) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Media> searchByFilename(@Param("ownerId") UUID ownerId,
                                  @Param("query") String query,
                                  Pageable pageable);

    /**
     * Delete all media for a tour
     */
    void deleteByTourId(UUID tourId);

    /**
     * Delete all media for a schedule
     */
    void deleteByScheduleId(UUID scheduleId);

    /**
     * Find media by tour ordered by display order
     */
    List<Media> findByTourIdOrderByDisplayOrderAsc(UUID tourId);

    /**
     * Find media by schedule ordered by display order
     */
    List<Media> findByScheduleIdOrderByDisplayOrderAsc(UUID scheduleId);

    /**
     * Get max display order for a tour's media
     */
    @Query("SELECT MAX(m.displayOrder) FROM Media m WHERE m.tour.id = :tourId")
    Integer getMaxDisplayOrderByTour(@Param("tourId") UUID tourId);

    /**
     * Get max display order for a schedule's media
     */
    @Query("SELECT MAX(m.displayOrder) FROM Media m WHERE m.schedule.id = :scheduleId")
    Integer getMaxDisplayOrderBySchedule(@Param("scheduleId") UUID scheduleId);

    /**
     * Unset hero image for all media in a tour
     */
    @Modifying
    @Query("UPDATE Media m SET m.isHero = false WHERE m.tour.id = :tourId AND m.isHero = true")
    void unsetHeroByTour(@Param("tourId") UUID tourId);
}
