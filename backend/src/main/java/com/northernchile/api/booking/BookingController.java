package com.northernchile.api.booking;

import com.northernchile.api.booking.dto.BookingClientUpdateReq;
import com.northernchile.api.booking.dto.BookingCreateReq;
import com.northernchile.api.booking.dto.BookingRes;
import com.northernchile.api.booking.dto.BookingUpdateReq;
import com.northernchile.api.config.security.annotation.CurrentUser;
import com.northernchile.api.model.User;
import com.northernchile.api.security.AuthorizationService;
import com.northernchile.api.security.Permission;
import com.northernchile.api.security.annotations.RequiresPermission;
import com.northernchile.api.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;

    public BookingController(
            BookingService bookingService,
            UserRepository userRepository,
            AuthorizationService authorizationService) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
    }

    // ==================== CLIENT ENDPOINTS ====================

    /**
     * Create a new booking (authenticated clients only)
     */
    @PostMapping("/bookings")
    @RequiresPermission(Permission.CREATE_BOOKING)
    public ResponseEntity<BookingRes> createBooking(@RequestBody BookingCreateReq req,
                                                    @CurrentUser User currentUser) {
        BookingRes createdBooking = bookingService.createBooking(req, currentUser);
        return new ResponseEntity<>(createdBooking, HttpStatus.CREATED);
    }

    /**
     * Get all bookings for the current user
     */
    @GetMapping("/bookings")
    @RequiresPermission(Permission.VIEW_BOOKING)
    public ResponseEntity<List<BookingRes>> getAllBookings(@CurrentUser User currentUser) {
        // Regular users see only their own bookings
        List<BookingRes> bookings = bookingService.getBookingsByUser(currentUser);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Get a specific booking (user can see their own bookings)
     */
    @GetMapping("/bookings/{bookingId}")
    @RequiresPermission(value = Permission.VIEW_BOOKING, resourceIdParam = "bookingId", resourceType = RequiresPermission.ResourceType.BOOKING)
    public ResponseEntity<BookingRes> getBookingById(@PathVariable UUID bookingId,
                                                    @CurrentUser User currentUser) {
        return bookingService.getBookingById(bookingId, currentUser)
                .map(booking -> new ResponseEntity<>(booking, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Confirm booking after mock payment (for testing)
     */
    @PostMapping("/bookings/{bookingId}/confirm-mock")
    @RequiresPermission(Permission.VIEW_BOOKING) // Basic auth, ownership checked in service
    public ResponseEntity<BookingRes> confirmMockPayment(@PathVariable UUID bookingId,
                                                        @CurrentUser User currentUser) {
        BookingRes confirmedBooking = bookingService.confirmBookingAfterMockPayment(bookingId, currentUser);
        return new ResponseEntity<>(confirmedBooking, HttpStatus.OK);
    }

    /**
     * Update booking details (client can update their own booking)
     */
    @PutMapping("/bookings/{bookingId}")
    @RequiresPermission(Permission.VIEW_BOOKING) // Basic auth, ownership checked in service
    public ResponseEntity<BookingRes> updateBookingDetails(
            @PathVariable UUID bookingId,
            @Valid @RequestBody BookingClientUpdateReq req,
            @CurrentUser User currentUser) {
        BookingRes updated = bookingService.updateBookingDetails(bookingId, req, currentUser);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    /**
     * Cancel booking (client can cancel their own booking)
     */
    @DeleteMapping("/bookings/{bookingId}")
    @RequiresPermission(Permission.CANCEL_BOOKING)
    public ResponseEntity<Void> cancelUserBooking(@PathVariable UUID bookingId,
                                                  @CurrentUser User currentUser) {
        bookingService.cancelBooking(bookingId, currentUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * Get all bookings for admin (filtered by ownership for Partner Admin)
     */
    @GetMapping("/admin/bookings")
    @RequiresPermission(Permission.VIEW_ALL_BOOKINGS)
    public ResponseEntity<List<BookingRes>> getAdminBookings(@CurrentUser User currentUser) {
        List<BookingRes> bookings = bookingService.getBookingsForAdmin(currentUser);
        return new ResponseEntity<>(bookings, HttpStatus.OK);
    }

    /**
     * Get a specific booking by ID (admin)
     */
    @GetMapping("/admin/bookings/{bookingId}")
    @RequiresPermission(value = Permission.VIEW_BOOKING, resourceIdParam = "bookingId", resourceType = RequiresPermission.ResourceType.BOOKING)
    public ResponseEntity<BookingRes> getAdminBookingById(@PathVariable UUID bookingId,
                                                        @CurrentUser User currentUser) {
        return bookingService.getBookingById(bookingId, currentUser)
                .map(booking -> new ResponseEntity<>(booking, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Update booking status (admin)
     */
    @PutMapping("/admin/bookings/{bookingId}")
    @RequiresPermission(value = Permission.UPDATE_BOOKING_STATUS, resourceIdParam = "bookingId", resourceType = RequiresPermission.ResourceType.BOOKING)
    public ResponseEntity<BookingRes> updateAdminBooking(
            @PathVariable UUID bookingId,
            @Valid @RequestBody BookingUpdateReq req,
            @CurrentUser User currentUser) {
        BookingRes updated = bookingService.updateBookingStatus(bookingId, req.status(), currentUser);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    /**
     * Cancel booking (admin)
     */
    @DeleteMapping("/admin/bookings/{bookingId}")
    @RequiresPermission(value = Permission.CANCEL_BOOKING, resourceIdParam = "bookingId", resourceType = RequiresPermission.ResourceType.BOOKING)
    public ResponseEntity<Void> cancelAdminBooking(@PathVariable UUID bookingId,
                                                   @CurrentUser User currentUser) {
        bookingService.cancelBooking(bookingId, currentUser);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
