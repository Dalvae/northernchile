package com.northernchile.api.alert.dto;

import com.northernchile.api.model.TourSchedule;
import com.northernchile.api.model.WeatherAlert;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface WeatherAlertMapper {

    @Mapping(target = "tourScheduleId", source = "tourSchedule.id")
    @Mapping(target = "title", source = ".", qualifiedByName = "generateTitle")
    @Mapping(target = "description", source = "message")
    @Mapping(target = "scheduleDate", source = "tourSchedule.startDatetime")
    @Mapping(target = "tourName", source = "tourSchedule.tour.nameTranslations.es")
    @Mapping(target = "moonIllumination", source = ".", qualifiedByName = "calculateMoonIllumination")
    WeatherAlertRes toRes(WeatherAlert alert);

    List<WeatherAlertRes> toResList(List<WeatherAlert> alerts);

    @Named("generateTitle")
    default String generateTitle(WeatherAlert alert) {
        if (alert == null || alert.getAlertType() == null) return "Alerta climática";

        TourSchedule schedule = alert.getTourSchedule();
        String tourName = "";
        if (schedule != null && schedule.getTour() != null) {
            var translations = schedule.getTour().getNameTranslations();
            tourName = translations != null ? translations.get("es") : "";
        }

        return switch (alert.getAlertType()) {
            case "WIND" -> "Alerta de Viento - " + (tourName != null ? tourName : "Tour");
            case "CLOUDS" -> "Alerta de Nubosidad - " + (tourName != null ? tourName : "Tour");
            case "MOON" -> "Alerta de Luna Llena - " + (tourName != null ? tourName : "Tour");
            default -> "Alerta Climática - " + (tourName != null ? tourName : "Tour");
        };
    }

    @Named("calculateMoonIllumination")
    default Integer calculateMoonIllumination(WeatherAlert alert) {
        if (alert == null || alert.getMoonPhase() == null) return null;
        // Moon phase is 0-1, convert to illumination percentage
        // 0 = new moon (0%), 0.5 = full moon (100%), 1 = new moon again (0%)
        double phase = alert.getMoonPhase();
        double illumination = (1 - Math.abs(2 * phase - 1)) * 100;
        return (int) Math.round(illumination);
    }

    default AlertHistoryRes toHistoryRes(List<WeatherAlert> alerts) {
        List<WeatherAlertRes> all = toResList(alerts);

        Map<UUID, List<WeatherAlertRes>> bySchedule = all.stream()
                .filter(a -> a.tourScheduleId() != null)
                .collect(Collectors.groupingBy(WeatherAlertRes::tourScheduleId));

        return new AlertHistoryRes(all, bySchedule);
    }
}
