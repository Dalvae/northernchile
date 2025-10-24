package com.northernchile.api.tour;

import com.northernchile.api.model.TourSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/schedules")
public class TourScheduleController {

    @Autowired
    private TourScheduleService tourScheduleService;

    @PostMapping
    public ResponseEntity<TourSchedule> createScheduledTour(@RequestBody TourSchedule tourSchedule) {
        TourSchedule createdSchedule = tourScheduleService.createScheduledTour(tourSchedule);
        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<TourSchedule> cancelScheduledTour(@PathVariable UUID id) {
        TourSchedule cancelledSchedule = tourScheduleService.cancelScheduledTour(id);
        if (cancelledSchedule != null) {
            return new ResponseEntity<>(cancelledSchedule, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<TourSchedule>> getSchedulesForMonth(
            @RequestParam UUID tourId,
            @RequestParam int year,
            @RequestParam int month) {
        List<TourSchedule> schedules = tourScheduleService.getScheduledToursForMonth(tourId, year, month);
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourSchedule> getTourScheduleById(@PathVariable UUID id) {
        TourSchedule schedule = tourScheduleService.getTourScheduleById(id);
        if (schedule != null) {
            return new ResponseEntity<>(schedule, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTourSchedule(@PathVariable UUID id) {
        tourScheduleService.deleteTourSchedule(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
