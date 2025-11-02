package com.northernchile.api.tour;

import com.northernchile.api.model.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, UUID> {
    List<TourSchedule> findByTourIdAndStartDatetimeBetween(UUID tourId, Instant start, Instant end);
    boolean existsByTourIdAndStartDatetime(UUID tourId, Instant startDatetime);
    boolean existsByTourIdAndStartDatetimeBetween(UUID tourId, Instant start, Instant end);
    List<TourSchedule> findByStartDatetimeBetween(Instant start, Instant end);

    /**
     * Find schedules with eagerly loaded Tour and Owner to avoid LazyInitializationException
     */
    @Query("SELECT s FROM TourSchedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "LEFT JOIN FETCH t.owner " +
           "WHERE s.startDatetime BETWEEN :start AND :end")
    List<TourSchedule> findByStartDatetimeBetweenWithTour(@Param("start") Instant start, @Param("end") Instant end);
}
