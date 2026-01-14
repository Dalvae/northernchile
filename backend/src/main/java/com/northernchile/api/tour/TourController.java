package com.northernchile.api.tour;

import com.northernchile.api.config.security.annotation.CurrentUser;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.User;
import com.northernchile.api.security.AuthorizationService;
import com.northernchile.api.security.Permission;
import com.northernchile.api.security.annotations.RequiresPermission;
import com.northernchile.api.tour.dto.TourCreateReq;
import com.northernchile.api.tour.dto.TourRes;
import com.northernchile.api.tour.dto.TourUpdateReq;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TourController {

    private final TourService tourService;
    private final TourRepository tourRepository;
    private final TourMapper tourMapper;
    private final AuthorizationService authorizationService;

    public TourController(
            TourService tourService,
            TourRepository tourRepository,
            TourMapper tourMapper,
            AuthorizationService authorizationService) {
        this.tourService = tourService;
        this.tourRepository = tourRepository;
        this.tourMapper = tourMapper;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/admin/tours")
    @RequiresPermission(Permission.CREATE_TOUR)
    public ResponseEntity<TourRes> createTour(@jakarta.validation.Valid @RequestBody TourCreateReq tourCreateReq,
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
    @RequiresPermission(Permission.VIEW_TOUR)
    public ResponseEntity<List<TourRes>> getAllToursForAdmin(@CurrentUser User currentUser) {
        // Super Admin can see all tours, Partner Admin can only see their own
        List<TourRes> tours;
        if (authorizationService.isSuperAdmin()) {
            tours = tourService.getAllToursForAdmin();
        } else {
            tours = tourService.getToursByOwner(currentUser);
        }
        return new ResponseEntity<>(tours, HttpStatus.OK);
    }

    @GetMapping("/admin/tours/{id}")
    @RequiresPermission(value = Permission.VIEW_TOUR, resourceIdParam = "id", resourceType = RequiresPermission.ResourceType.TOUR)
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
    @RequiresPermission(value = Permission.EDIT_TOUR, resourceIdParam = "id", resourceType = RequiresPermission.ResourceType.TOUR)
    public ResponseEntity<TourRes> updateTour(@PathVariable UUID id, @jakarta.validation.Valid @RequestBody TourUpdateReq tourUpdateReq,
                                              @CurrentUser User currentUser) {
        TourRes updatedTour = tourService.updateTour(id, tourUpdateReq, currentUser);
        return new ResponseEntity<>(updatedTour, HttpStatus.OK);
    }

    @DeleteMapping("/admin/tours/{id}")
    @RequiresPermission(value = Permission.DELETE_TOUR, resourceIdParam = "id", resourceType = RequiresPermission.ResourceType.TOUR)
    public ResponseEntity<Void> deleteTour(@PathVariable UUID id,
                                           @CurrentUser User currentUser) {
        tourService.deleteTour(id, currentUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
