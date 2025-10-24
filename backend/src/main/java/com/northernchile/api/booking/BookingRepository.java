package com.northernchile.api.booking;

import com.northernchile.api.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    @Query("SELECT COUNT(p) FROM Participant p WHERE p.booking.schedule.id = :scheduleId")
    Integer countParticipantsByScheduleId(UUID scheduleId);
}
