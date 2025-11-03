package com.northernchile.api.booking;

import com.northernchile.api.booking.dto.BookingCreateReq;
import com.northernchile.api.booking.dto.BookingRes;
import com.northernchile.api.booking.dto.BookingUpdateReq;
import com.northernchile.api.config.security.annotation.CurrentUser;
import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @Autowired
    public BookingController(BookingService bookingService, UserRepository userRepository) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingRes> createBooking(@RequestBody BookingCreateReq req,
                                                    @CurrentUser User currentUser) {
        BookingRes createdBooking = bookingService.createBooking(req, currentUser);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<BookingRes>> getAllBookings(@CurrentUser User currentUser) {
        List<BookingRes> bookings = bookingService.getAllBookings(currentUser);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<BookingRes> getBookingById(@PathVariable UUID bookingId,
                                                    @CurrentUser User currentUser) {
        return bookingService.getBookingById(bookingId, currentUser)
                .map(booking -> new ResponseEntity<>(booking, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/bookings/{bookingId}/confirm-mock")
    public ResponseEntity<BookingRes> confirmMockPayment(@PathVariable UUID bookingId,
                                                        @CurrentUser User currentUser) {
        BookingRes confirmedBooking = bookingService.confirmBookingAfterMockPayment(bookingId, currentUser);
        return new ResponseEntity<>(confirmedBooking, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')")
    @GetMapping("/admin/bookings")
    public ResponseEntity<List<BookingRes>> getAdminBookings(@CurrentUser User currentUser) {
        List<BookingRes> bookings = bookingService.getAllBookings(currentUser);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')")
    @GetMapping("/admin/bookings/{bookingId}")
    public ResponseEntity<BookingRes> getAdminBookingById(@PathVariable UUID bookingId,
                                                        @CurrentUser User currentUser) {
        return bookingService.getBookingById(bookingId, currentUser)
                .map(booking -> new ResponseEntity<>(booking, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')")
    @PutMapping("/admin/bookings/{bookingId}")
    public ResponseEntity<BookingRes> updateAdminBooking(
            @PathVariable UUID bookingId,
            @Valid @RequestBody BookingUpdateReq req,
            @CurrentUser User currentUser) {
        BookingRes updated = bookingService.updateBookingStatus(bookingId, req.getStatus(), currentUser);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'PARTNER_ADMIN')")
    @DeleteMapping("/admin/bookings/{bookingId}")
    public ResponseEntity<Void> cancelAdminBooking(@PathVariable UUID bookingId,
                                                   @CurrentUser User currentUser) {
        bookingService.cancelBooking(bookingId, currentUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}