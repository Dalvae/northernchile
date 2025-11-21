package com.northernchile.api.media.repository;

import com.northernchile.api.media.model.ScheduleMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ScheduleMedia join table.
 */
@Repository
public interface ScheduleMediaRepository extends JpaRepository<ScheduleMedia, ScheduleMedia.ScheduleMediaId> {

    /**
     * Find all media for a schedule ordered by display_order
     */
    List<ScheduleMedia> findByScheduleIdOrderByDisplayOrderAsc(UUID scheduleId);

    /**
     * Find all media for a schedule with eager fetch, ordered by display_order
     */
    @Query("SELECT sm FROM ScheduleMedia sm JOIN FETCH sm.media m JOIN FETCH m.owner WHERE sm.schedule.id = :scheduleId ORDER BY sm.displayOrder ASC")
    List<ScheduleMedia> findByScheduleIdWithMediaOrderByDisplayOrderAsc(@Param("scheduleId") UUID scheduleId);

    /**
     * Delete all media assignments for a schedule
     */
    void deleteByScheduleId(UUID scheduleId);

    /**
     * Delete a specific media assignment
     */
    void deleteByScheduleIdAndMediaId(UUID scheduleId, UUID mediaId);

    /**
     * Get max display order for a schedule
     */
    @Query("SELECT COALESCE(MAX(sm.displayOrder), -1) FROM ScheduleMedia sm WHERE sm.schedule.id = :scheduleId")
    Integer getMaxDisplayOrder(@Param("scheduleId") UUID scheduleId);
}
