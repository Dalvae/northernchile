package com.northernchile.api.tour;

import com.northernchile.api.model.Tour;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.dto.TourCreateReq;
import com.northernchile.api.tour.dto.TourRes;
import com.northernchile.api.tour.dto.TourUpdateReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TourService {

    @Autowired
    private TourRepository tourRepository;

    public TourRes createTour(TourCreateReq tourCreateReq, User currentUser) {
        Tour tour = new Tour();
        tour.setOwner(currentUser);
        tour.setName(tourCreateReq.getName());
        tour.setDescription(tourCreateReq.getDescription());
        tour.setCategory(tourCreateReq.getCategory());
        tour.setPriceAdult(tourCreateReq.getPriceAdult());
        tour.setPriceChild(tourCreateReq.getPriceChild());
        tour.setDefaultMaxParticipants(tourCreateReq.getDefaultMaxParticipants());
        tour.setDurationHours(tourCreateReq.getDurationHours());
        tour.setStatus("DRAFT"); // Default status

        Tour savedTour = tourRepository.save(tour);
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
                    tour.setName(tourUpdateReq.getName());
                    tour.setDescription(tourUpdateReq.getDescription());
                    tour.setCategory(tourUpdateReq.getCategory());
                    tour.setPriceAdult(tourUpdateReq.getPriceAdult());
                    tour.setPriceChild(tourUpdateReq.getPriceChild());
                    tour.setDefaultMaxParticipants(tourUpdateReq.getDefaultMaxParticipants());
                    tour.setDurationHours(tourUpdateReq.getDurationHours());
                    tour.setStatus(tourUpdateReq.getStatus());
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
        tourRes.setName(tour.getName());
        tourRes.setDescription(tour.getDescription());
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
}
