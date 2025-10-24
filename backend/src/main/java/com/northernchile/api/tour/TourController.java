package com.northernchile.api.tour;

import com.northernchile.api.model.User;
import com.northernchile.api.tour.dto.TourCreateReq;
import com.northernchile.api.tour.dto.TourRes;
import com.northernchile.api.tour.dto.TourUpdateReq;
import com.northernchile.api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    @Autowired
    private TourService tourService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TourRes> createTour(@RequestBody TourCreateReq tourCreateReq) {
        User currentUser = getCurrentUser();
        TourRes createdTour = tourService.createTour(tourCreateReq, currentUser);
        return new ResponseEntity<>(createdTour, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TourRes>> getAllTours() {
        List<TourRes> tours = tourService.getAllTours();
        return new ResponseEntity<>(tours, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourRes> getTourById(@PathVariable UUID id) {
        TourRes tour = tourService.getTourById(id);
        if (tour != null) {
            return new ResponseEntity<>(tour, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TourRes> updateTour(@PathVariable UUID id, @RequestBody TourUpdateReq tourUpdateReq) {
        User currentUser = getCurrentUser();
        TourRes updatedTour = tourService.updateTour(id, tourUpdateReq, currentUser);
        if (updatedTour != null) {
            return new ResponseEntity<>(updatedTour, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable UUID id) {
        tourService.deleteTour(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return userRepository.findByEmail(currentPrincipalName).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
