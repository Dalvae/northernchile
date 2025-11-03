package com.northernchile.api.config.security;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("bookingSecurityService")
public class BookingSecurityService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

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
        if (booking == null) {
            return false;
        }

        return booking.getSchedule().getTour().getOwner().getId().equals(currentUser.getId());
    }

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
        if (booking == null) {
            return false;
        }

        return booking.getUser().getId().equals(currentUser.getId());
    }
}
