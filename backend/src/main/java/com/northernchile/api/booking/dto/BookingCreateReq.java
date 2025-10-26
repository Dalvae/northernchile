
package com.northernchile.api.booking.dto;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BookingCreateReq {
    private UUID scheduleId;
    private List<ParticipantReq> participants;
    private String languageCode;
    private String specialRequests;

    public BookingCreateReq() {
    }

    public BookingCreateReq(UUID scheduleId, List<ParticipantReq> participants, String languageCode, String specialRequests) {
        this.scheduleId = scheduleId;
        this.participants = participants;
        this.languageCode = languageCode;
        this.specialRequests = specialRequests;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public List<ParticipantReq> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantReq> participants) {
        this.participants = participants;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookingCreateReq that = (BookingCreateReq) o;
        return Objects.equals(scheduleId, that.scheduleId) && Objects.equals(participants, that.participants) && Objects.equals(languageCode, that.languageCode) && Objects.equals(specialRequests, that.specialRequests);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduleId, participants, languageCode, specialRequests);
    }

    @Override
    public String toString() {
        return "BookingCreateReq{" +
                "scheduleId=" + scheduleId +
                ", participants=" + participants +
                ", languageCode='" + languageCode + ''' +
                ", specialRequests='" + specialRequests + ''' +
                '}';
    }
}
