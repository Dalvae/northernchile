package com.northernchile.api.tour;

import com.northernchile.api.model.TourImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TourImageRepository extends JpaRepository<TourImage, UUID> {
    List<TourImage> findByTourId(UUID tourId);
    void deleteByTourId(UUID tourId);
}