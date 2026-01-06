package com.northernchile.api.security;

import com.northernchile.api.model.OwnedEntity;
import com.northernchile.api.model.User;
import org.springframework.stereotype.Service;

@Service
public class ResourceOwnershipService {

    /**
     * Checks if the given user is the owner of the entity.
     *
     * @param user   The user to check.
     * @param entity The entity to check ownership of.
     * @return true if the user owns the entity, false otherwise.
     */
    public boolean isOwner(User user, OwnedEntity entity) {
        if (user == null || entity == null) {
            return false;
        }
        
        // If entity has no owner, it's effectively orphaned or public, but for security we default to false
        // unless logic dictates otherwise. Here we assume strict ownership.
        if (entity.getOwner() == null) {
            return false;
        }

        return entity.getOwner().getId().equals(user.getId());
    }
}
