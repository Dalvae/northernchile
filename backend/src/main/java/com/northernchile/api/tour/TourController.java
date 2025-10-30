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
@RequestMapping("/api") // Cambiar a /api para poder añadir /admin
public class TourController {

    @Autowired
    private TourService tourService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/admin/tours")
    public ResponseEntity<TourRes> createTour(@RequestBody TourCreateReq tourCreateReq) {
        User currentUser = getCurrentUser();
        TourRes createdTour = tourService.createTour(tourCreateReq, currentUser);
        return new ResponseEntity<>(createdTour, HttpStatus.CREATED);
    }

    @GetMapping("/tours") // <-- Endpoint Público
    public ResponseEntity<List<TourRes>> getPublishedTours() {
        List<TourRes> tours = tourService.getPublishedTours();
        return new ResponseEntity<>(tours, HttpStatus.OK);
    }
    
    @GetMapping("/admin/tours") // <-- NUEVO Endpoint de Admin
    public ResponseEntity<List<TourRes>> getAllToursForAdmin() {
        User currentUser = getCurrentUser();
        List<TourRes> tours = tourService.getAllTours(currentUser);
        return new ResponseEntity<>(tours, HttpStatus.OK);
    }

    @GetMapping("/admin/tours/{id}")
    public ResponseEntity<TourRes> getTourByIdAdmin(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        TourRes tour = tourService.getTourById(id, currentUser);
        return new ResponseEntity<>(tour, HttpStatus.OK);
    }

    @GetMapping("/tours/{id}") // Public endpoint - no ownership check needed
    public ResponseEntity<TourRes> getTourById(@PathVariable UUID id) {
        // For public endpoint, we create a temporary "public" user context
        // This endpoint should only show PUBLISHED tours
        User publicUser = new User();
        publicUser.setRole("ROLE_PUBLIC");
        TourRes tour = tourService.getTourById(id, publicUser);
        return new ResponseEntity<>(tour, HttpStatus.OK);
    }

    @PutMapping("/admin/tours/{id}")
    public ResponseEntity<TourRes> updateTour(@PathVariable UUID id, @RequestBody TourUpdateReq tourUpdateReq) {
        User currentUser = getCurrentUser();
        TourRes updatedTour = tourService.updateTour(id, tourUpdateReq, currentUser);
        return new ResponseEntity<>(updatedTour, HttpStatus.OK);
    }

    @DeleteMapping("/admin/tours/{id}")
    public ResponseEntity<Void> deleteTour(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        tourService.deleteTour(id, currentUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return userRepository.findByEmail(currentPrincipalName).orElseThrow(() -> new RuntimeException("User not found"));
    }
}
