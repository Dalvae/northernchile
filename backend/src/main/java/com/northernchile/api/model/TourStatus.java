package com.northernchile.api.model;

/**
 * Status values for tours.
 *
 * - DRAFT: Tour is being created/edited, not visible to public
 * - PUBLISHED: Tour is live and bookable by customers
 */
public enum TourStatus {
    DRAFT,
    PUBLISHED
}
