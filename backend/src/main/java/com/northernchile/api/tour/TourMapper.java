package com.northernchile.api.tour;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourImage;
import com.northernchile.api.tour.dto.ItineraryItem;
import com.northernchile.api.tour.dto.TourImageRes;
import com.northernchile.api.tour.dto.TourRes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface TourMapper {

    Logger log = LoggerFactory.getLogger(TourMapper.class);

    @Mapping(target = "guideName", source = "tour.guideName")
    @Mapping(target = "itinerary", expression = "java(extractItinerary(tour, locale))")
    @Mapping(target = "equipment", expression = "java(extractStringList(tour.getEquipmentTranslations(), locale, \"equipment\"))")
    @Mapping(target = "additionalInfo", expression = "java(extractStringList(tour.getAdditionalInfoTranslations(), locale, \"additionalInfo\"))")
    TourRes toTourRes(Tour tour, Locale locale);

    default TourRes toTourRes(Tour tour) {
        return toTourRes(tour, Locale.forLanguageTag("es"));
    }

    List<TourRes> toTourResList(List<Tour> tours);

    TourImageRes toTourImageRes(TourImage tourImage);

    /**
     * Extracts itinerary items for the requested language with fallback to Spanish.
     */
    default List<ItineraryItem> extractItinerary(Tour tour, Locale locale) {
        Map<String, Object> translations = tour.getItineraryTranslations();
        if (translations == null || translations.isEmpty()) {
            return null;
        }

        String lang = locale.getLanguage();
        Object itineraryObj = translations.get(lang);

        // Fallback to Spanish if requested language not found
        if (itineraryObj == null) {
            log.warn("Missing itinerary translation for locale '{}' in tour '{}'. Falling back to Spanish.", lang, tour.getId());
            itineraryObj = translations.get("es");
        }

        if (itineraryObj == null) {
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(itineraryObj, new TypeReference<List<ItineraryItem>>() {});
        } catch (Exception e) {
            log.error("Error parsing itinerary for tour '{}': {}", tour.getId(), e.getMessage());
            return null;
        }
    }

    /**
     * Extracts a list of strings for the requested language with fallback to Spanish.
     */
    default List<String> extractStringList(Map<String, Object> translations, Locale locale, String fieldName) {
        if (translations == null || translations.isEmpty()) {
            return null;
        }

        String lang = locale.getLanguage();
        Object listObj = translations.get(lang);

        // Fallback to Spanish if requested language not found
        if (listObj == null) {
            log.warn("Missing {} translation for locale '{}'. Falling back to Spanish.", fieldName, lang);
            listObj = translations.get("es");
        }

        if (listObj == null) {
            return null;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(listObj, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("Error parsing {} list: {}", fieldName, e.getMessage());
            return null;
        }
    }
}
