package com.northernchile.api.common;

import com.northernchile.api.model.User;
import com.northernchile.api.security.Role;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Utility class for creating JPA Specifications that filter by ownership/role.
 *
 * This centralizes the common pattern where SUPER_ADMIN sees all records
 * while PARTNER_ADMIN sees only records they own (directly or via relation).
 *
 * Usage examples:
 *
 * 1. Direct ownership (entity has ownerId field):
 * {@code
 *   Specification<Tour> spec = OwnershipSpecification.accessibleBy(user, "owner");
 *   repository.findAll(spec);
 * }
 *
 * 2. Indirect ownership (through relation):
 * {@code
 *   Specification<Booking> spec = OwnershipSpecification.accessibleByRelation(
 *       user, "schedule", "tour", "owner");
 *   repository.findAll(spec);
 * }
 */
public final class OwnershipSpecification {

    private OwnershipSpecification() {
        // Static utility class
    }

    /**
     * Creates a specification that filters by direct ownership.
     * SUPER_ADMIN sees all records, PARTNER_ADMIN sees only owned records.
     *
     * @param user The current user
     * @param ownerField The field name pointing to the owner User (e.g., "owner")
     * @param <T> The entity type
     * @return A specification that filters appropriately based on role
     */
    public static <T> Specification<T> accessibleBy(User user, String ownerField) {
        return (root, query, criteriaBuilder) -> {
            if (isSuperAdmin(user)) {
                return null; // No filter for SUPER_ADMIN
            }
            // PARTNER_ADMIN sees only their owned entities
            return criteriaBuilder.equal(root.get(ownerField).get("id"), user.getId());
        };
    }

    /**
     * Creates a specification that filters by direct owner ID.
     * Use this when the entity has a direct UUID ownerId field instead of a User relation.
     *
     * @param user The current user
     * @param ownerIdField The field name for the owner UUID (e.g., "ownerId")
     * @param <T> The entity type
     * @return A specification that filters appropriately based on role
     */
    public static <T> Specification<T> accessibleByOwnerId(User user, String ownerIdField) {
        return (root, query, criteriaBuilder) -> {
            if (isSuperAdmin(user)) {
                return null;
            }
            return criteriaBuilder.equal(root.get(ownerIdField), user.getId());
        };
    }

    /**
     * Creates a specification that filters by ownership through a single relation.
     * Example: Booking -> Schedule -> Tour -> Owner
     *
     * @param user The current user
     * @param relationField First relation field (e.g., "schedule")
     * @param ownerField The owner field on the related entity (e.g., "tour.owner")
     * @param <T> The entity type
     * @return A specification that filters appropriately based on role
     */
    public static <T> Specification<T> accessibleByRelation(
            User user, String relationField, String ownerField) {
        return (root, query, criteriaBuilder) -> {
            if (isSuperAdmin(user)) {
                return null;
            }
            Join<Object, Object> join = root.join(relationField);
            return criteriaBuilder.equal(join.get(ownerField).get("id"), user.getId());
        };
    }

    /**
     * Creates a specification that filters by ownership through two nested relations.
     * Example: Booking -> Schedule -> Tour -> Owner
     *
     * @param user The current user
     * @param relation1 First relation field (e.g., "schedule")
     * @param relation2 Second relation field (e.g., "tour")
     * @param ownerField The owner field on the final entity (e.g., "owner")
     * @param <T> The entity type
     * @return A specification that filters appropriately based on role
     */
    public static <T> Specification<T> accessibleByNestedRelation(
            User user, String relation1, String relation2, String ownerField) {
        return (root, query, criteriaBuilder) -> {
            if (isSuperAdmin(user)) {
                return null;
            }
            Join<Object, Object> join1 = root.join(relation1);
            Join<Object, Object> join2 = join1.join(relation2);
            return criteriaBuilder.equal(join2.get(ownerField).get("id"), user.getId());
        };
    }

    /**
     * Checks if a user is a SUPER_ADMIN.
     */
    public static boolean isSuperAdmin(User user) {
        return user != null && Role.SUPER_ADMIN.getRoleName().equals(user.getRole());
    }

    /**
     * Checks if a user is a PARTNER_ADMIN.
     */
    public static boolean isPartnerAdmin(User user) {
        return user != null && Role.PARTNER_ADMIN.getRoleName().equals(user.getRole());
    }

    /**
     * Checks if a user has any admin role (SUPER_ADMIN or PARTNER_ADMIN).
     */
    public static boolean isAdmin(User user) {
        return isSuperAdmin(user) || isPartnerAdmin(user);
    }
}
