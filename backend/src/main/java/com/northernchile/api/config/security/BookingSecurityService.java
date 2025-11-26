package com.northernchile.api.config.security;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("bookingSecurityService")
public class BookingSecurityService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public BookingSecurityService(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Check if the authenticated user is the owner of the tour associated with this booking
     * (i.e., the tour operator who can manage bookings for their tours)
     */
    public boolean isOwner(Authentication authentication, UUID bookingId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail).orElse(null);
        if (currentUser == null) {
            return false;
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
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
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail).orElse(null);
        if (currentUser == null) {
            return false;
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null || booking.getUser() == null) {
            return false;
        }

        return booking.getUser().getId().equals(currentUser.getId());
    }

    /**
     * Check if the user can view all bookings across the system
     * Only SUPER_ADMIN can view all bookings
     */
    public boolean canViewAllBookings(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return hasRole(authentication, "ROLE_SUPER_ADMIN");
    }

    /**
     * Check if the user can view bookings for their tours
     * SUPER_ADMIN can view all bookings
     * PARTNER_ADMIN can view bookings for their own tours
     */
    public boolean canViewTourBookings(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return hasAnyRole(authentication, "ROLE_SUPER_ADMIN", "ROLE_PARTNER_ADMIN");
    }

    /**
     * Check if the user can view a specific booking
     * SUPER_ADMIN can view any booking
     * PARTNER_ADMIN can view bookings for their tours
     * Regular users can view their own bookings
     */
    public boolean canViewBooking(Authentication authentication, UUID bookingId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // SUPER_ADMIN can view any booking
        if (hasRole(authentication, "ROLE_SUPER_ADMIN")) {
            return true;
        }

        // PARTNER_ADMIN can view bookings for their tours
        if (hasRole(authentication, "ROLE_PARTNER_ADMIN")) {
            return isOwner(authentication, bookingId);
        }

        // Regular users can view their own bookings
        return isBookingUser(authentication, bookingId);
    }

    /**
     * Check if the user can update a booking's status
     * SUPER_ADMIN can update any booking
     * PARTNER_ADMIN can only update bookings for their tours
     */
    public boolean canUpdateBooking(Authentication authentication, UUID bookingId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // SUPER_ADMIN can update any booking
        if (hasRole(authentication, "ROLE_SUPER_ADMIN")) {
            return true;
        }

        // PARTNER_ADMIN can only update bookings for their tours
        if (hasRole(authentication, "ROLE_PARTNER_ADMIN")) {
            return isOwner(authentication, bookingId);
        }

        return false;
    }

    /**
     * Check if the user can cancel a booking
     * SUPER_ADMIN can cancel any booking
     * PARTNER_ADMIN can cancel bookings for their tours
     * Regular users can cancel their own bookings
     */
    public boolean canCancelBooking(Authentication authentication, UUID bookingId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // SUPER_ADMIN can cancel any booking
        if (hasRole(authentication, "ROLE_SUPER_ADMIN")) {
            return true;
        }

        // PARTNER_ADMIN can cancel bookings for their tours
        if (hasRole(authentication, "ROLE_PARTNER_ADMIN")) {
            return isOwner(authentication, bookingId);
        }

        // Regular users can cancel their own bookings
        return isBookingUser(authentication, bookingId);
    }

    /**
     * Check if the user can create bookings
     * Any authenticated user can create bookings
     */
    public boolean canCreateBooking(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    // --- Helper methods ---

    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role));
    }

    private boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null) {
            return false;
        }
        for (String role : roles) {
            if (hasRole(authentication, role)) {
                return true;
            }
        }
        return false;
    }
}
