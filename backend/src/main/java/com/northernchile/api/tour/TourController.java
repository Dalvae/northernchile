package com.northernchile.api.tour;

import com.northernchile.api.config.security.annotation.CurrentUser;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.dto.TourCreateReq;
import com.northernchile.api.tour.dto.TourRes;
import com.northernchile.api.tour.dto.TourUpdateReq;
import com.northernchile.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TourController {

    private final TourService tourService;
    private final UserRepository userRepository;
    private final TourRepository tourRepository;
    private final TourMapper tourMapper;

    public TourController(
            TourService tourService,
            UserRepository userRepository,
            TourRepository tourRepository,
            TourMapper tourMapper) {
        this.tourService = tourService;
        this.userRepository = userRepository;
        this.tourRepository = tourRepository;
        this.tourMapper = tourMapper;
    }

    @PostMapping("/admin/tours")
    @PreAuthorize("@tourSecurityService.canCreateTour(authentication)")
    public ResponseEntity<TourRes> createTour(@RequestBody TourCreateReq tourCreateReq,
                                              @CurrentUser User currentUser) {
        TourRes createdTour = tourService.createTour(tourCreateReq, currentUser);
        return new ResponseEntity<>(createdTour, HttpStatus.CREATED);
    }

    @GetMapping("/tours")
    public ResponseEntity<List<TourRes>> getPublishedTours() {
        List<TourRes> tours = tourService.getPublishedTours();
        return new ResponseEntity<>(tours, HttpStatus.OK);
    }

    @GetMapping("/admin/tours")
    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN', 'ROLE_PARTNER_ADMIN')")
    public ResponseEntity<List<TourRes>> getAllToursForAdmin(@CurrentUser User currentUser) {
        // SUPER_ADMIN can see all tours, PARTNER_ADMIN can only see their own
        List<TourRes> tours;
        if (currentUser.getRole().equals("ROLE_SUPER_ADMIN")) {
            tours = tourService.getAllToursForAdmin();
        } else {
            tours = tourService.getToursByOwner(currentUser);
        }
        return new ResponseEntity<>(tours, HttpStatus.OK);
    }

    @GetMapping("/admin/tours/{id}")
    @PreAuthorize("@tourSecurityService.canViewTour(authentication, #id)")
    public ResponseEntity<TourRes> getTourByIdAdmin(@PathVariable UUID id,
                                                  @CurrentUser User currentUser) {
        TourRes tour = tourService.getTourById(id, currentUser);
        return new ResponseEntity<>(tour, HttpStatus.OK);
    }

    @GetMapping("/tours/slug/{slug}")
    public ResponseEntity<TourRes> getTourBySlug(@PathVariable String slug) {
        TourRes tour = tourService.getTourBySlug(slug);
        return new ResponseEntity<>(tour, HttpStatus.OK);
    }

    @GetMapping("/tours/{id}")
    public ResponseEntity<TourRes> getTourByIdPublic(@PathVariable UUID id) {
        Tour tour = tourRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new EntityNotFoundException("Tour not found with id: " + id));

        TourRes tourRes = tourMapper.toTourRes(tour);
        return new ResponseEntity<>(tourRes, HttpStatus.OK);
    }

    @PutMapping("/admin/tours/{id}")
    @PreAuthorize("@tourSecurityService.canEditTour(authentication, #id)")
    public ResponseEntity<TourRes> updateTour(@PathVariable UUID id, @RequestBody TourUpdateReq tourUpdateReq,
                                              @CurrentUser User currentUser) {
        TourRes updatedTour = tourService.updateTour(id, tourUpdateReq, currentUser);
        return new ResponseEntity<>(updatedTour, HttpStatus.OK);
    }

    @DeleteMapping("/admin/tours/{id}")
    @PreAuthorize("@tourSecurityService.canDeleteTour(authentication, #id)")
    public ResponseEntity<Void> deleteTour(@PathVariable UUID id,
                                           @CurrentUser User currentUser) {
        tourService.deleteTour(id, currentUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
