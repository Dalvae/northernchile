package com.northernchile.api.tour;

import com.northernchile.api.model.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, UUID> {
    List<TourSchedule> findByTourIdAndStartDatetimeBetween(UUID tourId, Instant start, Instant end);
    boolean existsByTourIdAndStartDatetime(UUID tourId, Instant startDatetime);
}
