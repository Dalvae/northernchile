package com.northernchile.api.config.security;

import com.northernchile.api.model.Tour;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.TourRepository;
import com.northernchile.api.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("tourSecurityService")
public class TourSecurityService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Check if the authenticated user is the owner of the tour
     */
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

    /**
     * Check if the user can create tours
     * Only SUPER_ADMIN and PARTNER_ADMIN can create tours
     */
    public boolean canCreateTour(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return hasAnyRole(authentication, "ROLE_SUPER_ADMIN", "ROLE_PARTNER_ADMIN");
    }

    /**
     * Check if the user can edit a specific tour
     * SUPER_ADMIN can edit any tour
     * PARTNER_ADMIN can only edit their own tours
     */
    public boolean canEditTour(Authentication authentication, UUID tourId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // SUPER_ADMIN can edit any tour
        if (hasRole(authentication, "ROLE_SUPER_ADMIN")) {
            return true;
        }

        // PARTNER_ADMIN can only edit their own tours
        if (hasRole(authentication, "ROLE_PARTNER_ADMIN")) {
            return isOwner(authentication, tourId);
        }

        return false;
    }

    /**
     * Check if the user can delete a specific tour
     * SUPER_ADMIN can delete any tour
     * PARTNER_ADMIN can only delete their own tours
     */
    public boolean canDeleteTour(Authentication authentication, UUID tourId) {
        // Same logic as edit for now
        return canEditTour(authentication, tourId);
    }

    /**
     * Check if the user can view all tours (including drafts from other owners)
     * Only SUPER_ADMIN can view all tours
     * PARTNER_ADMIN can only view their own tours
     */
    public boolean canViewAllTours(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return hasRole(authentication, "ROLE_SUPER_ADMIN");
    }

    /**
     * Check if the user can view a specific tour
     * SUPER_ADMIN can view any tour
     * PARTNER_ADMIN can only view their own tours
     * Regular users can only view published tours
     */
    public boolean canViewTour(Authentication authentication, UUID tourId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // SUPER_ADMIN can view any tour
        if (hasRole(authentication, "ROLE_SUPER_ADMIN")) {
            return true;
        }

        // PARTNER_ADMIN can view their own tours
        if (hasRole(authentication, "ROLE_PARTNER_ADMIN")) {
            return isOwner(authentication, tourId);
        }

        // Regular users can only view published tours
        Tour tour = tourRepository.findById(tourId).orElse(null);
        if (tour == null) {
            return false;
        }

        return "PUBLISHED".equals(tour.getStatus());
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
