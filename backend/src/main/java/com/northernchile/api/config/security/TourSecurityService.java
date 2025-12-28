package com.northernchile.api.config.security;

import com.northernchile.api.model.Tour;
import com.northernchile.api.model.User;
import com.northernchile.api.tour.TourRepository;
import com.northernchile.api.user.UserRepository;
import com.northernchile.api.util.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("tourSecurityService")
public class TourSecurityService {

    private final TourRepository tourRepository;
    private final UserRepository userRepository;

    public TourSecurityService(TourRepository tourRepository, UserRepository userRepository) {
        this.tourRepository = tourRepository;
        this.userRepository = userRepository;
    }

    /**
     * Check if the authenticated user is the owner of the tour
     */
    public boolean isOwner(Authentication authentication, UUID tourId) {
        if (!SecurityUtils.isAuthenticated(authentication)) {
            return false;
        }
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail).orElse(null);
        if (currentUser == null) {
            return false;
        }

        Tour tour = tourRepository.findById(tourId).orElse(null);
        if (tour == null || tour.getOwner() == null) {
            return false;
        }

        return tour.getOwner().getId().equals(currentUser.getId());
    }

    /**
     * Check if the user can create tours
     * Only SUPER_ADMIN and PARTNER_ADMIN can create tours
     */
    public boolean canCreateTour(Authentication authentication) {
        return SecurityUtils.isAdmin(authentication);
    }

    /**
     * Check if the user can edit a specific tour
     * SUPER_ADMIN can edit any tour
     * PARTNER_ADMIN can only edit their own tours
     */
    public boolean canEditTour(Authentication authentication, UUID tourId) {
        if (!SecurityUtils.isAuthenticated(authentication)) {
            return false;
        }

        if (SecurityUtils.isSuperAdmin(authentication)) {
            return true;
        }

        if (SecurityUtils.isPartnerAdmin(authentication)) {
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
        return canEditTour(authentication, tourId);
    }

    /**
     * Check if the user can view all tours (including drafts from other owners)
     * Only SUPER_ADMIN can view all tours
     */
    public boolean canViewAllTours(Authentication authentication) {
        return SecurityUtils.isSuperAdmin(authentication);
    }

    /**
     * Check if the user can view a specific tour
     * SUPER_ADMIN can view any tour
     * PARTNER_ADMIN can only view their own tours
     * Regular users can only view published tours
     */
    public boolean canViewTour(Authentication authentication, UUID tourId) {
        if (!SecurityUtils.isAuthenticated(authentication)) {
            return false;
        }

        if (SecurityUtils.isSuperAdmin(authentication)) {
            return true;
        }

        if (SecurityUtils.isPartnerAdmin(authentication)) {
            return isOwner(authentication, tourId);
        }

        Tour tour = tourRepository.findById(tourId).orElse(null);
        if (tour == null) {
            return false;
        }

        return "PUBLISHED".equals(tour.getStatus());
    }
}
