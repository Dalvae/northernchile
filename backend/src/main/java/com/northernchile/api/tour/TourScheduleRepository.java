package com.northernchile.api.tour;

import com.northernchile.api.model.TourSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, UUID> {
    List<TourSchedule> findByTourIdAndStartDatetimeBetweenOrderByStartDatetimeDesc(UUID tourId, Instant start, Instant end);
    boolean existsByTourIdAndStartDatetime(UUID tourId, Instant startDatetime);
    boolean existsByTourIdAndStartDatetimeBetween(UUID tourId, Instant start, Instant end);
    List<TourSchedule> findByStartDatetimeBetween(Instant start, Instant end);

    /**
     * Find schedule by ID with pessimistic write lock.
     * Use this when checking availability and creating bookings to prevent race conditions.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TourSchedule s WHERE s.id = :id")
    Optional<TourSchedule> findByIdWithLock(@Param("id") UUID id);

    /**
     * Find schedules with eagerly loaded Tour and Owner to avoid LazyInitializationException
     */
    @Query("SELECT s FROM TourSchedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "LEFT JOIN FETCH t.owner " +
           "WHERE s.startDatetime BETWEEN :start AND :end")
    List<TourSchedule> findByStartDatetimeBetweenWithTour(@Param("start") Instant start, @Param("end") Instant end);

    /**
     * Find OPEN schedules that start before the cutoff time (for auto-close job)
     */
    @Query("SELECT s FROM TourSchedule s " +
           "LEFT JOIN FETCH s.tour " +
           "WHERE s.status = 'OPEN' AND s.startDatetime <= :cutoff")
    List<TourSchedule> findOpenSchedulesBeforeCutoff(@Param("cutoff") Instant cutoff);

    /**
     * Find schedule by ID with eagerly loaded Tour and Owner.
     * Use this for ownership checks to avoid LazyInitializationException.
     */
    @Query("SELECT s FROM TourSchedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "LEFT JOIN FETCH t.owner " +
           "WHERE s.id = :id")
    Optional<TourSchedule> findByIdWithTourAndOwner(@Param("id") UUID id);

    /**
     * Batch query to find all existing schedule dates for multiple tours.
     * Returns tour ID and start datetime pairs for efficient in-memory lookup.
     * Used by TourScheduleGeneratorService to avoid N+1 queries.
     */
    @Query("SELECT s.tour.id, s.startDatetime FROM TourSchedule s " +
           "WHERE s.tour.id IN :tourIds AND s.startDatetime BETWEEN :start AND :end")
    List<Object[]> findExistingScheduleDatesByTourIds(
        @Param("tourIds") List<UUID> tourIds,
        @Param("start") Instant start,
        @Param("end") Instant end
    );
}
