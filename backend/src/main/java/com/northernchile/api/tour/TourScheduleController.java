package com.northernchile.api.tour;

import com.northernchile.api.model.User;
import com.northernchile.api.tour.dto.TourScheduleCreateReq;
import com.northernchile.api.tour.dto.TourScheduleRes;
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
@RequestMapping("/api/admin/schedules")
public class TourScheduleController {

    @Autowired
    private TourScheduleService tourScheduleService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TourScheduleRes> createScheduledTour(@RequestBody TourScheduleCreateReq req) {
        User currentUser = getCurrentUser();
        TourScheduleRes createdSchedule = tourScheduleService.createScheduledTour(req, currentUser);
        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<TourScheduleRes> cancelScheduledTour(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
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
    public ResponseEntity<Void> deleteTourSchedule(@PathVariable UUID id) {
        User currentUser = getCurrentUser();
        tourScheduleService.deleteTourSchedule(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return userRepository.findByEmail(currentPrincipalName)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
