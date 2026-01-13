package com.northernchile.api.booking;

import com.northernchile.api.model.Booking;
import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Security service for booking-related authorization checks.
 * Used by @PreAuthorize expressions in controllers to verify 
 * that the authenticated user owns the booking they're trying to access.
 */
@Service("bookingSecurityService")
public class BookingSecurityService {

    private static final Logger log = LoggerFactory.getLogger(BookingSecurityService.class);

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public BookingSecurityService(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    /**
     * Check if the authenticated user is the owner of the specified booking.
     * Used by @PreAuthorize annotations to secure user-specific booking operations.
     *
     * @param authentication The current authentication object
     * @param bookingId The booking ID to check ownership for
     * @return true if the authenticated user owns the booking, false otherwise
     */
    public boolean isBookingUser(Authentication authentication, UUID bookingId) {
        // Check if user is authenticated
        if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("isBookingUser check failed: User not authenticated");
            return false;
        }

        // Get the current user from database
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email).orElse(null);
        
        if (currentUser == null) {
            log.warn("isBookingUser check failed: User not found for email: {}", email);
            return false;
        }

        // Find the booking and check ownership
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        
        if (booking == null) {
            log.warn("isBookingUser check failed: Booking not found: {}", bookingId);
            return false;
        }

        // Check if the booking belongs to the current user
        boolean isOwner = booking.getUser() != null 
                && booking.getUser().getId().equals(currentUser.getId());
        
        if (!isOwner) {
            log.warn("isBookingUser check failed: User {} does not own booking {}", 
                    currentUser.getId(), bookingId);
        } else {
            log.debug("isBookingUser check passed: User {} owns booking {}", 
                    currentUser.getId(), bookingId);
        }

        return isOwner;
    }
}


