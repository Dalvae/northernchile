package com.northernchile.api.tour;

import com.northernchile.api.model.Tour;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourRepository extends JpaRepository<Tour, UUID> {
    List<Tour> findByStatus(String status);
    List<Tour> findByRecurringAndStatus(boolean recurring, String status);
    List<Tour> findByRecurringTrueAndStatus(String status);

    // Queries excluding soft-deleted tours
    @Query("SELECT t FROM Tour t WHERE t.deletedAt IS NULL")
    List<Tour> findAllNotDeleted();

    @Query("SELECT t FROM Tour t WHERE t.id = :id AND t.deletedAt IS NULL")
    Optional<Tour> findByIdNotDeleted(@Param("id") UUID id);

    @Query("SELECT t FROM Tour t WHERE t.status = :status AND t.deletedAt IS NULL")
    List<Tour> findByStatusNotDeleted(@Param("status") String status);

    // Queries for multi-tenant filtering (PARTNER_ADMIN only sees their own tours)
    @Query("SELECT t FROM Tour t WHERE t.owner.id = :ownerId AND t.deletedAt IS NULL")
    List<Tour> findByOwnerIdNotDeleted(@Param("ownerId") UUID ownerId);

    @Query("SELECT t FROM Tour t WHERE t.id = :id AND t.owner.id = :ownerId AND t.deletedAt IS NULL")
    Optional<Tour> findByIdAndOwnerIdNotDeleted(@Param("id") UUID id, @Param("ownerId") UUID ownerId);

    // Slug-based queries
    @Query("SELECT t FROM Tour t WHERE t.slug = :slug AND t.deletedAt IS NULL")
    Optional<Tour> findBySlugNotDeleted(@Param("slug") String slug);

    @Query("SELECT t FROM Tour t WHERE t.slug = :slug AND t.status = 'PUBLISHED' AND t.deletedAt IS NULL")
    Optional<Tour> findBySlugPublished(@Param("slug") String slug);

    Optional<Tour> findBySlug(String slug);

    // Queries with EntityGraph to avoid N+1 (eagerly fetch owner)
    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT t FROM Tour t WHERE t.status = :status AND t.deletedAt IS NULL")
    List<Tour> findByStatusNotDeletedWithImages(@Param("status") String status);

    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT t FROM Tour t WHERE t.deletedAt IS NULL")
    List<Tour> findAllNotDeletedWithImages();

    @EntityGraph(attributePaths = {"owner"})
    @Query("SELECT t FROM Tour t WHERE t.owner.id = :ownerId AND t.deletedAt IS NULL")
    List<Tour> findByOwnerIdNotDeletedWithImages(@Param("ownerId") UUID ownerId);
}
