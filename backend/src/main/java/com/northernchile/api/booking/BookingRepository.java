package com.northernchile.api.booking;

import com.northernchile.api.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    /**
     * Count confirmed participants for a schedule.
     * Note: Locking should be done on the TourSchedule (via findByIdWithLock),
     * not on this count query. The schedule lock prevents race conditions.
     */
    @Query("SELECT COUNT(p) FROM Participant p WHERE p.booking.schedule.id = :scheduleId AND p.booking.status = 'CONFIRMED'")
    Integer countConfirmedParticipantsByScheduleId(UUID scheduleId);

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.booking.schedule.id = :scheduleId")
    Integer countParticipantsByScheduleId(UUID scheduleId);

    List<Booking> findByScheduleId(UUID scheduleId);

    /**
     * Find all bookings for a schedule with full details (for cascade cancellation).
     */
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.schedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "LEFT JOIN FETCH b.user " +
           "LEFT JOIN FETCH b.participants " +
           "WHERE s.id = :scheduleId")
    List<Booking> findByScheduleIdWithDetails(@Param("scheduleId") UUID scheduleId);

    List<Booking> findByCreatedAtBetween(Instant start, Instant end);

    /**
     * Find bookings in date range with tour info eagerly loaded (for reports).
     * Avoids N+1 queries when accessing schedule.tour.nameTranslations.
     */
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.schedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "LEFT JOIN FETCH b.participants " +
           "WHERE b.createdAt BETWEEN :start AND :end")
    List<Booking> findByCreatedAtBetweenWithTourInfo(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT b FROM Booking b " +
           "LEFT JOIN FETCH b.schedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "LEFT JOIN FETCH t.owner " +
           "LEFT JOIN FETCH b.user " +
           "LEFT JOIN FETCH b.participants " +
           "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(UUID id);

    /**
     * Paginated version of findAllWithDetails for admin listing.
     */
    @Query(value = "SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.schedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "LEFT JOIN FETCH t.owner " +
           "LEFT JOIN FETCH b.user " +
           "LEFT JOIN FETCH b.participants",
           countQuery = "SELECT COUNT(DISTINCT b) FROM Booking b")
    Page<Booking> findAllWithDetailsPaged(Pageable pageable);

    /**
     * Paginated bookings filtered by tour owner for partner admin listing.
     */
    @Query(value = "SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.schedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "LEFT JOIN FETCH t.owner o " +
           "LEFT JOIN FETCH b.user " +
           "LEFT JOIN FETCH b.participants " +
           "WHERE o.id = :ownerId",
           countQuery = "SELECT COUNT(DISTINCT b) FROM Booking b " +
           "JOIN b.schedule s JOIN s.tour t JOIN t.owner o WHERE o.id = :ownerId")
    Page<Booking> findByTourOwnerIdPaged(@Param("ownerId") UUID ownerId, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.schedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "LEFT JOIN FETCH t.owner " +
           "LEFT JOIN FETCH b.user u " +
           "LEFT JOIN FETCH b.participants " +
           "WHERE u.id = :userId")
    List<Booking> findByUserId(UUID userId);

    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.schedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "LEFT JOIN FETCH b.user u " +
           "LEFT JOIN FETCH b.participants " +
           "WHERE b.status = :status AND s.startDatetime BETWEEN :start AND :end")
    List<Booking> findByStatusAndTourSchedule_StartDateTimeBetween(String status, Instant start, Instant end);

    /**
     * Find confirmed bookings for upcoming tours that haven't received a reminder yet
     */
    @Query("SELECT DISTINCT b FROM Booking b " +
           "LEFT JOIN FETCH b.schedule s " +
           "LEFT JOIN FETCH s.tour t " +
           "LEFT JOIN FETCH b.user u " +
           "LEFT JOIN FETCH b.participants " +
           "WHERE b.status = :status AND s.startDatetime BETWEEN :start AND :end AND b.reminderSentAt IS NULL")
    List<Booking> findByStatusAndStartDateTimeBetweenAndReminderNotSent(
            @Param("status") String status,
            @Param("start") Instant start,
            @Param("end") Instant end);

    /**
     * Find stale pending bookings (older than specified time) for cleanup
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' AND b.createdAt < :cutoff")
    List<Booking> findStalePendingBookings(@Param("cutoff") Instant cutoff);
}
