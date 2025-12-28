package com.northernchile.api.model;

import java.util.UUID;

/**
 * Interface for entities that have an owner.
 * Used for consistent ownership validation across the application.
 */
public interface OwnedEntity {
    
    /**
     * Get the owner of this entity.
     * @return the owner User
     */
    User getOwner();
    
    /**
     * Get the ID of this entity.
     * @return the entity ID
     */
    UUID getId();
}
