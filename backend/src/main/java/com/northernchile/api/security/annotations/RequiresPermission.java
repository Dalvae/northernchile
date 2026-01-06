package com.northernchile.api.security.annotations;

import com.northernchile.api.security.Permission;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce permission checks on methods or classes.
 * 
 * Usage examples:
 * 
 * 1. Simple permission check:
 *    @RequiresPermission(Permission.VIEW_TOUR)
 * 
 * 2. Multiple permissions (ANY - user needs at least one):
 *    @RequiresPermission(value = {Permission.VIEW_TOUR, Permission.VIEW_ALL_TOURS}, mode = PermissionMode.ANY)
 * 
 * 3. Multiple permissions (ALL - user needs all):
 *    @RequiresPermission(value = {Permission.VIEW_TOUR, Permission.EDIT_TOUR}, mode = PermissionMode.ALL)
 * 
 * 4. Resource-based check (checks ownership via parameter):
 *    @RequiresPermission(value = Permission.EDIT_TOUR, resourceIdParam = "tourId")
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    
    /**
     * The permission(s) required to access this method.
     */
    Permission[] value();
    
    /**
     * How multiple permissions should be evaluated.
     * ANY = user needs at least one permission (OR logic)
     * ALL = user needs all permissions (AND logic)
     */
    PermissionMode mode() default PermissionMode.ANY;
    
    /**
     * Optional: Name of the method parameter containing the resource ID.
     * When specified, the aspect will also check if the user owns the resource.
     * Super Admin bypasses this check.
     */
    String resourceIdParam() default "";
    
    /**
     * Optional: The entity type for resource-based checks.
     * Used with resourceIdParam to determine how to fetch and check ownership.
     */
    ResourceType resourceType() default ResourceType.NONE;
    
    enum PermissionMode {
        ANY,  // User needs at least one of the permissions
        ALL   // User needs all of the permissions
    }
    
    enum ResourceType {
        NONE,
        TOUR,
        BOOKING,
        SCHEDULE,
        MEDIA,
        USER
    }
}
