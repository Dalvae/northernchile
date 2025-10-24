package com.northernchile.api.tour;

import com.northernchile.api.tour.dto.TourCreateReq;
import com.northernchile.api.tour.dto.TourRes;
import com.northernchile.api.tour.dto.TourUpdateReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tours")
public class TourController {

    @Autowired
    private TourService tourService;

    @PostMapping
    public ResponseEntity<TourRes> createTour(@RequestBody TourCreateReq tourCreateReq) {
        TourRes createdTour = tourService.createTour(tourCreateReq);
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
        TourRes updatedTour = tourService.updateTour(id, tourUpdateReq);
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
}
