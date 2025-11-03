package com.northernchile.api.config.security;

import com.northernchile.api.model.Tour;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.TourRepository;
import com.northernchile.api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("tourSecurityService")
public class TourSecurityService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private UserRepository userRepository;

    public boolean isOwner(Authentication authentication, UUID tourId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail).orElse(null);
        if (currentUser == null) {
            return false;
        }

        Tour tour = tourRepository.findById(tourId).orElse(null);
        if (tour == null) {
            return false;
        }

        return tour.getOwner().getId().equals(currentUser.getId());
    }
}
