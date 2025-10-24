package com.northernchile.api.booking;

import com.northernchile.api.booking.dto.BookingCreateReq;
import com.northernchile.api.booking.dto.BookingRes;
import com.northernchile.api.model.Booking;
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

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @Autowired
    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<BookingRes> createBooking(@RequestBody BookingCreateReq req) {
        User currentUser = getCurrentUser();
        BookingRes createdBooking = bookingService.createBooking(req, currentUser);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BookingRes>> getAllBookings() {
        List<BookingRes> bookings = bookingService.getAllBookings();
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingRes> getBookingById(@PathVariable UUID bookingId) {
        return bookingService.getBookingById(bookingId)
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