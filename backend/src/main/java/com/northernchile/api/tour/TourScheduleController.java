package com.northernchile.api.tour;

import com.northernchile.api.config.security.annotation.CurrentUser;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.dto.TourScheduleCreateReq;
import com.northernchile.api.tour.dto.TourScheduleRes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
public class TourScheduleController {

    private final TourScheduleService tourScheduleService;

    public TourScheduleController(TourScheduleService tourScheduleService) {
        this.tourScheduleService = tourScheduleService;
    }

    @PostMapping
    public ResponseEntity<TourScheduleRes> createScheduledTour(@RequestBody TourScheduleCreateReq req,
                                                               @CurrentUser User currentUser) {
        TourScheduleRes createdSchedule = tourScheduleService.createScheduledTour(req, currentUser);
        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<TourScheduleRes> cancelScheduledTour(@PathVariable UUID id,
                                                               @CurrentUser User currentUser) {
        TourScheduleRes cancelledSchedule = tourScheduleService.cancelScheduledTour(id, currentUser);
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
    public ResponseEntity<Void> deleteTourSchedule(@PathVariable UUID id,
                                           @CurrentUser User currentUser) {
        tourScheduleService.deleteTourSchedule(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
