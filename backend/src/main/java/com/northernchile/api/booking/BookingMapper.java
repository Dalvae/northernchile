package com.northernchile.api.booking;

import com.northernchile.api.booking.dto.BookingRes;
import com.northernchile.api.booking.dto.ParticipantRes;
import com.northernchile.api.model.Booking;
import com.northernchile.api.model.Participant;
import com.northernchile.api.util.DateTimeUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "schedule.tour.nameTranslations", target = "tourName")
    @Mapping(source = "schedule.startDatetime", target = "tourDate")
    @Mapping(source = "schedule.startDatetime", target = "tourStartTime")
    @Mapping(source = "schedule.id", target = "scheduleId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")
    @Mapping(source = "user.phoneNumber", target = "userPhoneNumber")
    BookingRes toBookingRes(Booking booking);

    List<BookingRes> toBookingResList(List<Booking> bookings);

    ParticipantRes toParticipantRes(Participant participant);

    List<ParticipantRes> toParticipantResList(List<Participant> participants);

    default String mapTourName(Map<String, String> nameTranslations) {
        return nameTranslations != null ? nameTranslations.get("es") : null;
    }

    default LocalDate mapInstantToLocalDate(Instant instant) {
        return DateTimeUtils.toLocalDate(instant);
    }

    default LocalTime mapInstantToLocalTime(Instant instant) {
        return DateTimeUtils.toLocalTime(instant);
    }
}
