package com.northernchile.api.tour.mapper;

import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.tour.dto.TourScheduleRes;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Centralized mapper for TourSchedule -> TourScheduleRes conversion.
 * Reduces duplication across TourScheduleService, TourSchedulePublicController,
 * and TourScheduleAdminController.
 */
@Component
public class TourScheduleMapper {

    /**
     * Maps a TourSchedule entity to TourScheduleRes DTO.
     * Availability information must be calculated by the caller since
     * different contexts use different calculation strategies.
     *
     * @param schedule The TourSchedule entity
     * @param bookedParticipants Number of booked participants
     * @param availableSlots Number of available slots
     * @return TourScheduleRes DTO
     */
    public TourScheduleRes toRes(TourSchedule schedule, int bookedParticipants, int availableSlots) {
        UUID tourId = null;
        String tourName = null;
        Map<String, String> tourNameTranslations = null;
        Integer tourDurationHours = null;

        if (schedule.getTour() != null) {
            tourId = schedule.getTour().getId();
            tourDurationHours = schedule.getTour().getDurationHours();
            if (schedule.getTour().getNameTranslations() != null) {
                tourName = schedule.getTour().getNameTranslations().get("es");
                tourNameTranslations = schedule.getTour().getNameTranslations();
            }
        }

        UUID assignedGuideId = null;
        String assignedGuideName = null;
        if (schedule.getAssignedGuide() != null) {
            assignedGuideId = schedule.getAssignedGuide().getId();
            assignedGuideName = schedule.getAssignedGuide().getFullName();
        }

        return new TourScheduleRes(
                schedule.getId(),
                tourId,
                tourName,
                tourNameTranslations,
                tourDurationHours,
                schedule.getStartDatetime(),
                schedule.getMaxParticipants(),
                bookedParticipants,
                availableSlots,
                schedule.getStatus(),
                assignedGuideId,
                assignedGuideName,
                schedule.getCreatedAt()
        );
    }
}
