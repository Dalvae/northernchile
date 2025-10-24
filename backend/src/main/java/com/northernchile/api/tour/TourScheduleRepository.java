package com.northernchile.api.tour;

import com.northernchile.api.model.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, UUID> {
}
