package com.northernchile.api.tour;

import com.northernchile.api.tour.dto.TourScheduleCreateReq;
import com.northernchile.api.tour.dto.TourScheduleRes;
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
    public ResponseEntity<TourScheduleRes> createScheduledTour(@RequestBody TourScheduleCreateReq req) {
        TourScheduleRes createdSchedule = tourScheduleService.createScheduledTour(req);
        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<TourScheduleRes> cancelScheduledTour(@PathVariable UUID id) {
        TourScheduleRes cancelledSchedule = tourScheduleService.cancelScheduledTour(id);
        return ResponseEntity.ok(cancelledSchedule);
    }

    @GetMapping
    public ResponseEntity<List<TourScheduleRes>> getSchedulesForMonth(
            @RequestParam UUID tourId,
            @RequestParam int year,
            @RequestParam int month) {
        List<TourScheduleRes> schedules = tourScheduleService.getScheduledToursForMonth(tourId, year, month);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TourScheduleRes> getTourScheduleById(@PathVariable UUID id) {
        TourScheduleRes schedule = tourScheduleService.getTourScheduleById(id);
        return ResponseEntity.ok(schedule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTourSchedule(@PathVariable UUID id) {
        tourScheduleService.deleteTourSchedule(id);
        return ResponseEntity.noContent().build();
    }
}
