package com.northernchile.api.booking;

import com.northernchile.api.booking.dto.BookingCreateReq;
import com.northernchile.api.booking.dto.BookingRes;
import com.northernchile.api.model.User;
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
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository; // For basic CRUD

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<BookingRes> createBooking(@RequestBody BookingCreateReq req) {
        User currentUser = getCurrentUser();
        BookingRes createdBooking = bookingService.createBooking(req, currentUser);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    // These are basic CRUD endpoints for admin purposes.
    // A real application would have more sophisticated logic and security.

    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        return new ResponseEntity<>(bookingRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable UUID id) {
        return bookingRepository.findById(id)
                .map(booking -> new ResponseEntity<>(booking, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        return userRepository.findByEmail(currentPrincipalName)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentPrincipalName));
    }
}
