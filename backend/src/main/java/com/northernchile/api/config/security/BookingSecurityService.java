package com.northernchile.api.config.security;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import com.northernchile.api.util.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("bookingSecurityService")
@Transactional(readOnly = true)
public class BookingSecurityService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public BookingSecurityService(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Check if the authenticated user is the owner of the tour associated with this booking
     */
    public boolean isOwner(Authentication authentication, UUID bookingId) {
        if (!SecurityUtils.isAuthenticated(authentication)) {
            return false;
        }
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail).orElse(null);
        if (currentUser == null) {
            return false;
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId).orElse(null);
        if (booking == null || booking.getSchedule() == null ||
            booking.getSchedule().getTour() == null ||
            booking.getSchedule().getTour().getOwner() == null) {
            return false;
        }

        return booking.getSchedule().getTour().getOwner().getId().equals(currentUser.getId());
    }

    /**
     * Check if the authenticated user is the customer who made this booking
     */
    public boolean isBookingUser(Authentication authentication, UUID bookingId) {
        if (!SecurityUtils.isAuthenticated(authentication)) {
            return false;
        }
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail).orElse(null);
        if (currentUser == null) {
            return false;
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId).orElse(null);
        if (booking == null || booking.getUser() == null) {
            return false;
        }

        return booking.getUser().getId().equals(currentUser.getId());
    }

    /**
     * Check if the user can view all bookings across the system
     */
    public boolean canViewAllBookings(Authentication authentication) {
        return SecurityUtils.isSuperAdmin(authentication);
    }

    /**
     * Check if the user can view bookings for their tours
     */
    public boolean canViewTourBookings(Authentication authentication) {
        return SecurityUtils.isAdmin(authentication);
    }

    /**
     * Check if the user can view a specific booking
     */
    public boolean canViewBooking(Authentication authentication, UUID bookingId) {
        if (!SecurityUtils.isAuthenticated(authentication)) {
            return false;
        }

        if (SecurityUtils.isSuperAdmin(authentication)) {
            return true;
        }

        if (SecurityUtils.isPartnerAdmin(authentication)) {
            return isOwner(authentication, bookingId);
        }

        return isBookingUser(authentication, bookingId);
    }

    /**
     * Check if the user can update a booking's status
     */
    public boolean canUpdateBooking(Authentication authentication, UUID bookingId) {
        if (!SecurityUtils.isAuthenticated(authentication)) {
            return false;
        }

        if (SecurityUtils.isSuperAdmin(authentication)) {
            return true;
        }

        if (SecurityUtils.isPartnerAdmin(authentication)) {
            return isOwner(authentication, bookingId);
        }

        return false;
    }

    /**
     * Check if the user can cancel a booking
     */
    public boolean canCancelBooking(Authentication authentication, UUID bookingId) {
        if (!SecurityUtils.isAuthenticated(authentication)) {
            return false;
        }

        if (SecurityUtils.isSuperAdmin(authentication)) {
            return true;
        }

        if (SecurityUtils.isPartnerAdmin(authentication)) {
            return isOwner(authentication, bookingId);
        }

        return isBookingUser(authentication, bookingId);
    }

    /**
     * Check if the user can create bookings
     */
    public boolean canCreateBooking(Authentication authentication) {
        return SecurityUtils.isAuthenticated(authentication);
    }
}
