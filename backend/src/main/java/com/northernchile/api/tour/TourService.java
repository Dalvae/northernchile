package com.northernchile.api.tour;

import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourImage;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.dto.TourCreateReq;
import com.northernchile.api.tour.dto.TourRes;
import com.northernchile.api.tour.dto.TourUpdateReq;
import com.northernchile.api.tour.dto.TourImageRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TourService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private TourImageRepository tourImageRepository;

    public TourRes createTour(TourCreateReq tourCreateReq, User currentUser) {
        Tour tour = new Tour();
        tour.setOwner(currentUser);
        tour.setNameTranslations(tourCreateReq.getNameTranslations());
        tour.setDescriptionTranslations(tourCreateReq.getDescriptionTranslations());
        tour.setWindSensitive(tourCreateReq.isWindSensitive());
        tour.setMoonSensitive(tourCreateReq.isMoonSensitive());
        tour.setCloudSensitive(tourCreateReq.isCloudSensitive());
        tour.setCategory(tourCreateReq.getCategory());
        tour.setPriceAdult(tourCreateReq.getPriceAdult());
        tour.setPriceChild(tourCreateReq.getPriceChild());
        tour.setDefaultMaxParticipants(tourCreateReq.getDefaultMaxParticipants());
        tour.setDurationHours(tourCreateReq.getDurationHours());
        tour.setStatus("DRAFT"); // Default status

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

        return toTourResponse(savedTour);
    }

    public List<TourRes> getAllTours() {
        return tourRepository.findAll().stream()
                .map(this::toTourResponse)
                .collect(Collectors.toList());
    }

    public TourRes getTourById(UUID id) {
        return tourRepository.findById(id)
                .map(this::toTourResponse)
                .orElse(null); // Or throw an exception
    }

    public TourRes updateTour(UUID id, TourUpdateReq tourUpdateReq, User currentUser) {
        return tourRepository.findById(id)
                .map(tour -> {
                    if (!tour.getOwner().getId().equals(currentUser.getId()) && !currentUser.getRole().equals("ROLE_SUPER_ADMIN")) {
                        throw new AccessDeniedException("You do not have permission to edit this tour.");
                    }
                    tour.setNameTranslations(tourUpdateReq.getNameTranslations());
                    tour.setDescriptionTranslations(tourUpdateReq.getDescriptionTranslations());
                    tour.setWindSensitive(tourUpdateReq.isWindSensitive());
                    tour.setMoonSensitive(tourUpdateReq.isMoonSensitive());
                    tour.setCloudSensitive(tourUpdateReq.isCloudSensitive());
                    tour.setCategory(tourUpdateReq.getCategory());
                    tour.setPriceAdult(tourUpdateReq.getPriceAdult());
                    tour.setPriceChild(tourUpdateReq.getPriceChild());
                    tour.setDefaultMaxParticipants(tourUpdateReq.getDefaultMaxParticipants());
                    tour.setDurationHours(tourUpdateReq.getDurationHours());
                    tour.setStatus(tourUpdateReq.getStatus());

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
                    return toTourResponse(updatedTour);
                })
                .orElse(null); // Or throw an exception
    }

    public void deleteTour(UUID id) {
        tourRepository.deleteById(id);
    }

    private TourRes toTourResponse(Tour tour) {
        TourRes tourRes = new TourRes();
        tourRes.setId(tour.getId());
        tourRes.setNameTranslations(tour.getNameTranslations());
        tourRes.setDescriptionTranslations(tour.getDescriptionTranslations());
        tourRes.setImages(tour.getImages().stream().map(this::toTourImageRes).collect(Collectors.toList()));
        tourRes.setMoonSensitive(tour.isMoonSensitive());
        tourRes.setWindSensitive(tour.isWindSensitive());
        tourRes.setCloudSensitive(tour.isCloudSensitive());
        tourRes.setCategory(tour.getCategory());
        tourRes.setPriceAdult(tour.getPriceAdult());
        tourRes.setPriceChild(tour.getPriceChild());
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
}
