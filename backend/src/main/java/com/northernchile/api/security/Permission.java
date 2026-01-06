package com.northernchile.api.security;

/**
 * Enum defining granular permissions for the application.
 * Permissions are mapped to roles in AuthorizationService.
 */
public enum Permission {
    // ==================== TOUR PERMISSIONS ====================
    CREATE_TOUR("tour:create"),
    VIEW_TOUR("tour:view"),
    EDIT_TOUR("tour:edit"),
    DELETE_TOUR("tour:delete"),
    VIEW_ALL_TOURS("tour:view:all"),
    MANAGE_ALL_TOURS("tour:manage:all"),

    // ==================== SCHEDULE PERMISSIONS ====================
    CREATE_SCHEDULE("schedule:create"),
    VIEW_SCHEDULE("schedule:view"),
    EDIT_SCHEDULE("schedule:edit"),
    DELETE_SCHEDULE("schedule:delete"),
    VIEW_ALL_SCHEDULES("schedule:view:all"),

    // ==================== BOOKING PERMISSIONS ====================
    CREATE_BOOKING("booking:create"),
    VIEW_BOOKING("booking:view"),
    VIEW_ALL_BOOKINGS("booking:view:all"),
    CANCEL_BOOKING("booking:cancel"),
    UPDATE_BOOKING_STATUS("booking:status:update"),
    MANAGE_ALL_BOOKINGS("booking:manage:all"),

    // ==================== MEDIA PERMISSIONS ====================
    UPLOAD_MEDIA("media:upload"),
    VIEW_MEDIA("media:view"),
    DELETE_MEDIA("media:delete"),
    MANAGE_ALL_MEDIA("media:manage:all"),

    // ==================== STORAGE PERMISSIONS ====================
    UPLOAD_FILE("storage:upload"),
    DELETE_FILE("storage:delete"),
    MANAGE_STORAGE("storage:manage"),

    // ==================== USER PERMISSIONS ====================
    VIEW_USERS("user:view"),
    CREATE_USER("user:create"),
    EDIT_USER("user:edit"),
    DELETE_USER("user:delete"),
    MANAGE_USERS("user:manage:all"),

    // ==================== CONTACT/ADMIN PERMISSIONS ====================
    VIEW_CONTACT_MESSAGES("contact:view"),
    MANAGE_CONTACT_MESSAGES("contact:manage"),
    VIEW_PRIVATE_TOUR_REQUESTS("private_tour:view"),
    MANAGE_PRIVATE_TOUR_REQUESTS("private_tour:manage"),

    // ==================== DASHBOARD PERMISSIONS ====================
    VIEW_ADMIN_DASHBOARD("dashboard:view"),
    VIEW_REPORTS("reports:view"),
    VIEW_AUDIT_LOGS("audit:view");

    private final String code;

    Permission(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
