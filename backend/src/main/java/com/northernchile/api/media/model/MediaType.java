package com.northernchile.api.media.model;

/**
 * Type of media based on its association.
 */
public enum MediaType {
    TOUR,      // Media belongs to a tour
    SCHEDULE,  // Media belongs to a specific schedule
    LOOSE      // Media not assigned to any tour or schedule
}
