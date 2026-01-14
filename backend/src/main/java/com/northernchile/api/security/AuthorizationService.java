package com.northernchile.api.security;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.OwnedEntity;
import com.northernchile.api.model.User;
import com.northernchile.api.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

/**
 * Central service for authorization checks.
 * Provides methods to check permissions based on user roles and resource ownership.
 */
@Service("authorizationService")
public class AuthorizationService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationService.class);

    private final ResourceOwnershipService ownershipService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public AuthorizationService(ResourceOwnershipService ownershipService,
                                UserRepository userRepository,
                                BookingRepository bookingRepository) {
        this.ownershipService = ownershipService;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    // ==================== USER CONTEXT ====================

    /**
     * Get the current authenticated user from the database.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        // authentication.getName() returns the email (username) in our system
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());
    }

    // ==================== ROLE CHECKS ====================

    public boolean hasRole(Role role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role.getRoleName()));
    }

    public boolean isSuperAdmin() {
        return hasRole(Role.SUPER_ADMIN);
    }

    public boolean isPartnerAdmin() {
        return hasRole(Role.PARTNER_ADMIN);
    }

    public boolean isClient() {
        return hasRole(Role.CLIENT);
    }

    public boolean isAdmin() {
        return isSuperAdmin() || isPartnerAdmin();
    }

    // ==================== PERMISSION CHECKS ====================

    /**
     * Check if the current user has the specified permission.
     * Super Admin has all permissions.
     * Partner Admin has a subset of permissions (own resources).
     * Client has limited permissions (own profile, own bookings).
     */
    public boolean hasPermission(Permission permission) {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }

        // Super Admin has all permissions
        if (isSuperAdmin()) {
            return true;
        }

        // Partner Admin permissions
        if (isPartnerAdmin()) {
            return PARTNER_ADMIN_PERMISSIONS.contains(permission);
        }

        // Client permissions
        if (isClient()) {
            return CLIENT_PERMISSIONS.contains(permission);
        }

        return false;
    }

    /**
     * Throws AccessDeniedException if user doesn't have the permission.
     */
    public void checkPermission(Permission permission) {
        if (!hasPermission(permission)) {
            throw new AccessDeniedException("Access Denied: Missing permission " + permission);
        }
    }

    /**
     * Check if user has ANY of the specified permissions.
     */
    public boolean hasAnyPermission(Permission... permissions) {
        for (Permission permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user has ALL of the specified permissions.
     */
    public boolean hasAllPermissions(Permission... permissions) {
        for (Permission permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    // ==================== OWNERSHIP CHECKS ====================

    public void checkOwnership(OwnedEntity entity) {
        checkOwnership(entity, "Access Denied: You do not own this resource.");
    }

    public void checkOwnership(OwnedEntity entity, String message) {
        if (!canAccess(entity)) {
            throw new AccessDeniedException(message);
        }
    }

    public boolean canAccess(OwnedEntity entity) {
        User user = getCurrentUser();
        if (user == null) {
            return false;
        }

        if (isSuperAdmin()) {
            return true;
        }

        return ownershipService.isOwner(user, entity);
    }

    // ==================== BOOKING OWNERSHIP ====================

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

    // ==================== PERMISSION SETS BY ROLE ====================

    /**
     * Permissions granted to Partner Admins.
     * Partner Admins can manage their own resources but not all resources.
     */
    private static final Set<Permission> PARTNER_ADMIN_PERMISSIONS = Set.of(
            // Tour permissions (own tours)
            Permission.CREATE_TOUR,
            Permission.VIEW_TOUR,
            Permission.EDIT_TOUR,
            Permission.DELETE_TOUR,

            // Schedule permissions (own schedules)
            Permission.CREATE_SCHEDULE,
            Permission.VIEW_SCHEDULE,
            Permission.EDIT_SCHEDULE,
            Permission.DELETE_SCHEDULE,

            // Booking permissions (own tour's bookings)
            Permission.VIEW_BOOKING,
            Permission.VIEW_ALL_BOOKINGS, // Filtered by ownership in service
            Permission.CANCEL_BOOKING,
            Permission.UPDATE_BOOKING_STATUS,

            // Media permissions (own media)
            Permission.UPLOAD_MEDIA,
            Permission.VIEW_MEDIA,
            Permission.DELETE_MEDIA,

            // Storage permissions
            Permission.UPLOAD_FILE,
            Permission.DELETE_FILE,
            Permission.MANAGE_STORAGE,

            // Admin dashboard
            Permission.VIEW_ADMIN_DASHBOARD,
            Permission.VIEW_REPORTS,

            // Contact and private tour requests
            Permission.VIEW_CONTACT_MESSAGES,
            Permission.MANAGE_CONTACT_MESSAGES,
            Permission.VIEW_PRIVATE_TOUR_REQUESTS,
            Permission.MANAGE_PRIVATE_TOUR_REQUESTS
    );

    /**
     * Permissions granted to Clients.
     * Clients can create bookings and manage their own profile.
     */
    private static final Set<Permission> CLIENT_PERMISSIONS = Set.of(
            Permission.CREATE_BOOKING,
            Permission.VIEW_BOOKING, // Own bookings only (enforced by ownership check)
            Permission.CANCEL_BOOKING, // Own bookings only
            Permission.VIEW_TOUR,
            Permission.VIEW_SCHEDULE
    );
}
