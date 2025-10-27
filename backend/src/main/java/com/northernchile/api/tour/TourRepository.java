package com.northernchile.api.tour;

import com.northernchile.api.model.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TourRepository extends JpaRepository<Tour, UUID> {
    List<Tour> findByStatus(String status); // <-- AÑADIR ESTO
    List<Tour> findByIsRecurringAndStatus(boolean isRecurring, String status);
}
