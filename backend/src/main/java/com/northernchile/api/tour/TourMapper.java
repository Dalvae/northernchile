package com.northernchile.api.tour;

import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourImage;
import com.northernchile.api.tour.dto.TourImageRes;
import com.northernchile.api.tour.dto.TourRes;
import com.northernchile.api.tour.dto.ItineraryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface TourMapper {

    Logger log = LoggerFactory.getLogger(TourMapper.class);

    @Mappings({
            @Mapping(target = "id", source = "tour.id"),
            @Mapping(target = "slug", source = "tour.slug"),
            @Mapping(target = "nameTranslations", source = "tour.nameTranslations"),
            @Mapping(target = "category", source = "tour.category"),
            @Mapping(target = "price", source = "tour.price"),
            @Mapping(target = "defaultMaxParticipants", source = "tour.defaultMaxParticipants"),
            @Mapping(target = "durationHours", source = "tour.durationHours"),
            @Mapping(target = "defaultStartTime", source = "tour.defaultStartTime"),
            @Mapping(target = "status", source = "tour.status"),
            @Mapping(target = "images", source = "tour.images"),
            @Mapping(target = "moonSensitive", source = "tour.moonSensitive"),
            @Mapping(target = "windSensitive", source = "tour.windSensitive"),
            @Mapping(target = "cloudSensitive", source = "tour.cloudSensitive"),
            @Mapping(target = "createdAt", source = "tour.createdAt"),
            @Mapping(target = "updatedAt", source = "tour.updatedAt"),
            @Mapping(target = "contentKey", source = "tour.contentKey"),
            @Mapping(target = "guideName", source = "tour.guideName"),
            @Mapping(target = "descriptionBlocksTranslations", source = "tour.descriptionBlocksTranslations"),
            @Mapping(target = "itinerary", expression = "java(mapItinerary(tour.getItineraryTranslations(), locale))"),
            @Mapping(target = "equipment", expression = "java(mapStringList(tour.getEquipmentTranslations(), locale))"),
            @Mapping(target = "additionalInfo", expression = "java(mapStringList(tour.getAdditionalInfoTranslations(), locale))")
    })
    TourRes toTourRes(Tour tour, Locale locale);

    // Helpers para contenido estructurado
    default java.util.List<ItineraryItem> mapItinerary(Map<String, Object> translations, Locale locale) {
        if (translations == null || translations.isEmpty()) return java.util.Collections.emptyList();
        Object localized = translations.getOrDefault(locale.getLanguage(), translations.get("es"));
        if (!(localized instanceof java.util.List<?> list)) return java.util.Collections.emptyList();
        java.util.List<ItineraryItem> result = new java.util.ArrayList<>();
        for (Object o : list) {
            if (o instanceof java.util.Map<?, ?> m) {
                Object time = m.get("time");
                Object description = m.get("description");
                if (time != null && description != null) {
                    ItineraryItem item = new ItineraryItem();
                    item.setTime(time.toString());
                    item.setDescription(description.toString());
                    result.add(item);
                }
            }
        }
        return result;
    }

    default java.util.List<String> mapStringList(Map<String, Object> translations, Locale locale) {
        if (translations == null || translations.isEmpty()) return java.util.Collections.emptyList();
        Object localized = translations.getOrDefault(locale.getLanguage(), translations.get("es"));
        if (!(localized instanceof java.util.List<?> list)) return java.util.Collections.emptyList();
        java.util.List<String> result = new java.util.ArrayList<>();
        for (Object o : list) {
            if (o != null) {
                result.add(o.toString());
            }
        }
        return result;
    }



    default TourRes toTourRes(Tour tour) {
        return toTourRes(tour, Locale.forLanguageTag("es"));
    }

    List<TourRes> toTourResList(List<Tour> tours);
 
    TourImageRes toTourImageRes(TourImage tourImage);

}
