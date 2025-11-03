package com.northernchile.api.tour;

import com.northernchile.api.config.security.annotation.CurrentUser;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.dto.TourCreateReq;
import com.northernchile.api.tour.dto.TourRes;
import com.northernchile.api.tour.dto.TourUpdateReq;
import com.northernchile.api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class TourController {

    @Autowired
    private TourService tourService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/admin/tours")
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
    public ResponseEntity<List<TourRes>> getAllToursForAdmin(@CurrentUser User currentUser) {
        List<TourRes> tours = tourService.getAllTours(currentUser);
        return new ResponseEntity<>(tours, HttpStatus.OK);
    }

    @GetMapping("/admin/tours/{id}")
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
    public ResponseEntity<TourRes> getTourById(@PathVariable UUID id) {
        User publicUser = new User();
        publicUser.setRole("ROLE_PUBLIC");
        TourRes tour = tourService.getTourById(id, publicUser);
        return new ResponseEntity<>(tour, HttpStatus.OK);
    }

    @PutMapping("/admin/tours/{id}")
    public ResponseEntity<TourRes> updateTour(@PathVariable UUID id, @RequestBody TourUpdateReq tourUpdateReq,
                                              @CurrentUser User currentUser) {
        TourRes updatedTour = tourService.updateTour(id, tourUpdateReq, currentUser);
        return new ResponseEntity<>(updatedTour, HttpStatus.OK);
    }

    @DeleteMapping("/admin/tours/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable UUID id,
                                           @CurrentUser User currentUser) {
        tourService.deleteTour(id, currentUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
