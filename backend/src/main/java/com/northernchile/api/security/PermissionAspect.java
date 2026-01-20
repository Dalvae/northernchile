package com.northernchile.api.security;

import com.northernchile.api.booking.BookingRepository;
import com.northernchile.api.media.repository.MediaRepository;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.User;
import com.northernchile.api.security.annotations.RequiresPermission;
import com.northernchile.api.tour.TourRepository;
import com.northernchile.api.tour.TourScheduleRepository;
import com.northernchile.api.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;

/**
 * Aspect that intercepts methods annotated with @RequiresPermission
 * and enforces permission checks using AuthorizationService.
 */
@Aspect
@Component
@Order(1) // Run before other aspects
public class PermissionAspect {

    private static final Logger log = LoggerFactory.getLogger(PermissionAspect.class);

    private final AuthorizationService authorizationService;
    private final TourRepository tourRepository;
    private final TourScheduleRepository scheduleRepository;
    private final BookingRepository bookingRepository;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;

    public PermissionAspect(
            AuthorizationService authorizationService,
            TourRepository tourRepository,
            TourScheduleRepository scheduleRepository,
            BookingRepository bookingRepository,
            MediaRepository mediaRepository,
            UserRepository userRepository) {
        this.authorizationService = authorizationService;
        this.tourRepository = tourRepository;
        this.scheduleRepository = scheduleRepository;
        this.bookingRepository = bookingRepository;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
    }

    @Before("@annotation(requiresPermission)")
    public void checkPermission(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        log.debug("Checking permissions for method: {}", joinPoint.getSignature().getName());

        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        // Super Admin bypasses all permission checks
        if (authorizationService.isSuperAdmin()) {
            log.debug("Super Admin access granted for method: {}", joinPoint.getSignature().getName());
            return;
        }

        User currentUser = authorizationService.getCurrentUser();
        if (currentUser == null) {
            log.warn("User not found in security context for authenticated request to method: {}", joinPoint.getSignature().getName());
            throw new AccessDeniedException("User not found in security context");
        }

        Permission[] permissions = requiresPermission.value();
        RequiresPermission.PermissionMode mode = requiresPermission.mode();

        // Check permission(s)
        boolean hasPermission = checkPermissions(permissions, mode);

        if (!hasPermission) {
            log.warn("Access denied for user {} to method {}. Required permissions: {}",
                    currentUser.getEmail(), joinPoint.getSignature().getName(), permissions);
            throw new AccessDeniedException("Access Denied: Insufficient permissions");
        }

        // Check resource ownership if specified
        if (!requiresPermission.resourceIdParam().isEmpty()
                && requiresPermission.resourceType() != RequiresPermission.ResourceType.NONE) {
            checkResourceOwnership(joinPoint, requiresPermission, currentUser);
        }

        log.debug("Permission check passed for user {} on method {}",
                currentUser.getEmail(), joinPoint.getSignature().getName());
    }

    /**
     * Check if the current user has the required permissions based on the mode.
     */
    private boolean checkPermissions(Permission[] permissions, RequiresPermission.PermissionMode mode) {
        if (mode == RequiresPermission.PermissionMode.ALL) {
            // User needs ALL permissions
            for (Permission permission : permissions) {
                if (!authorizationService.hasPermission(permission)) {
                    return false;
                }
            }
            return true;
        } else {
            // User needs ANY permission (default)
            for (Permission permission : permissions) {
                if (authorizationService.hasPermission(permission)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Check if the user owns the specified resource.
     */
    private void checkResourceOwnership(JoinPoint joinPoint, RequiresPermission requiresPermission, User currentUser) {
        String paramName = requiresPermission.resourceIdParam();
        RequiresPermission.ResourceType resourceType = requiresPermission.resourceType();

        // Get the resource ID from the method parameters
        UUID resourceId = extractResourceId(joinPoint, paramName);
        if (resourceId == null) {
            log.error("Security: Could not extract resource ID from parameter '{}' - denying access", paramName);
            throw new AccessDeniedException("Access Denied: Unable to verify resource ownership");
        }

        boolean isOwner = checkOwnership(resourceId, resourceType, currentUser);
        if (!isOwner) {
            log.warn("User {} does not own {} with ID {}",
                    currentUser.getEmail(), resourceType, resourceId);
            throw new AccessDeniedException("Access Denied: You do not own this resource");
        }
    }

    /**
     * Extract the resource ID from method parameters by name.
     */
    private UUID extractResourceId(JoinPoint joinPoint, String paramName) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        // First try by parameter name
        String[] parameterNames = methodSignature.getParameterNames();
        if (parameterNames != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                if (parameterNames[i].equals(paramName) && args[i] != null) {
                    return convertToUUID(args[i]);
                }
            }
        }

        // Fallback: try by index (if parameter has @PathVariable or @RequestParam with matching name)
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(paramName) && args[i] != null) {
                return convertToUUID(args[i]);
            }
        }

        return null;
    }

    private UUID convertToUUID(Object value) {
        if (value instanceof UUID) {
            return (UUID) value;
        } else if (value instanceof String) {
            try {
                return UUID.fromString((String) value);
            } catch (IllegalArgumentException e) {
                log.warn("Could not convert {} to UUID", value);
                return null;
            }
        }
        return null;
    }

    /**
     * Check if the user owns the resource based on type.
     */
    private boolean checkOwnership(UUID resourceId, RequiresPermission.ResourceType resourceType, User currentUser) {
        return switch (resourceType) {
            case TOUR -> {
                Tour tour = tourRepository.findById(resourceId)
                        .orElseThrow(() -> new EntityNotFoundException("Tour not found: " + resourceId));
                yield tour.getOwner() != null && tour.getOwner().getId().equals(currentUser.getId());
            }
            case SCHEDULE -> {
                TourSchedule schedule = scheduleRepository.findByIdWithTourAndOwner(resourceId)
                        .orElseThrow(() -> new EntityNotFoundException("Schedule not found: " + resourceId));
                Tour tour = schedule.getTour();
                yield tour != null && tour.getOwner() != null && tour.getOwner().getId().equals(currentUser.getId());
            }
            case BOOKING -> {
                Booking booking = bookingRepository.findById(resourceId)
                        .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + resourceId));
                // Partner Admin owns booking if they own the tour
                Tour tour = booking.getSchedule().getTour();
                yield tour != null && tour.getOwner() != null && tour.getOwner().getId().equals(currentUser.getId());
            }
            case MEDIA -> {
                var media = mediaRepository.findById(resourceId)
                        .orElseThrow(() -> new EntityNotFoundException("Media not found: " + resourceId));
                yield media.getOwner() != null && media.getOwner().getId().equals(currentUser.getId());
            }
            case USER -> {
                // User can only manage themselves (Super Admin already bypassed)
                yield resourceId.equals(currentUser.getId());
            }
            case NONE -> true; // No ownership check needed
        };
    }
}
