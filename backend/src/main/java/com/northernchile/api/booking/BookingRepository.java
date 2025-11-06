package com.northernchile.api.booking;

import com.northernchile.api.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @Query("SELECT COUNT(p) FROM Participant p WHERE p.booking.schedule.id = :scheduleId AND p.booking.status = 'CONFIRMED'")
    Integer countConfirmedParticipantsByScheduleId(UUID scheduleId);

    @Query("SELECT COUNT(p) FROM Participant p WHERE p.booking.schedule.id = :scheduleId")
    Integer countParticipantsByScheduleId(UUID scheduleId);

    List<Booking> findByScheduleId(UUID scheduleId);

    List<Booking> findByCreatedAtBetween(Instant start, Instant end);
}
