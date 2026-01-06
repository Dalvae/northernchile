package com.northernchile.api.tour;

import com.northernchile.api.model.Tour;
import com.northernchile.api.media.model.Media;
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

import com.northernchile.api.storage.S3StorageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class TourMapper {

    protected static final Logger log = LoggerFactory.getLogger(TourMapper.class);

    @Autowired
    protected S3StorageService s3StorageService;

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
            @Mapping(target = "images", ignore = true), // Images loaded separately via /gallery endpoint
            @Mapping(target = "isMoonSensitive", source = "tour.moonSensitive"),
            @Mapping(target = "isWindSensitive", source = "tour.windSensitive"),
            @Mapping(target = "isCloudSensitive", source = "tour.cloudSensitive"),
            @Mapping(target = "createdAt", source = "tour.createdAt"),
            @Mapping(target = "updatedAt", source = "tour.updatedAt"),
            @Mapping(target = "contentKey", source = "tour.contentKey"),
            @Mapping(target = "guideName", source = "tour.guideName"),
            @Mapping(target = "descriptionBlocksTranslations", source = "tour.descriptionBlocksTranslations"),
            @Mapping(target = "itinerary", expression = "java(mapItinerary(tour.getItineraryTranslations(), locale))"),
            @Mapping(target = "equipment", expression = "java(mapStringList(tour.getEquipmentTranslations(), locale))"),
            @Mapping(target = "additionalInfo", expression = "java(mapStringList(tour.getAdditionalInfoTranslations(), locale))"),
            @Mapping(target = "itineraryTranslations", expression = "java(mapAllItineraryTranslations(tour.getItineraryTranslations()))"),
            @Mapping(target = "equipmentTranslations", expression = "java(mapAllStringListTranslations(tour.getEquipmentTranslations()))"),
            @Mapping(target = "additionalInfoTranslations", expression = "java(mapAllStringListTranslations(tour.getAdditionalInfoTranslations()))"),
            @Mapping(target = "ownerId", source = "tour.owner.id"),
            @Mapping(target = "ownerName", source = "tour.owner.fullName"),
            @Mapping(target = "ownerEmail", source = "tour.owner.email")
    })
    public abstract TourRes toTourRes(Tour tour, Locale locale);

    // Helpers para contenido estructurado
    public java.util.List<ItineraryItem> mapItinerary(Map<String, Object> translations, Locale locale) {
        if (translations == null || translations.isEmpty()) return java.util.Collections.emptyList();
        Object localized = translations.getOrDefault(locale.getLanguage(), translations.get("es"));
        if (!(localized instanceof java.util.List<?> list)) return java.util.Collections.emptyList();
        java.util.List<ItineraryItem> result = new java.util.ArrayList<>();
        for (Object o : list) {
            if (o instanceof java.util.Map<?, ?> m) {
                Object time = m.get("time");
                Object description = m.get("description");
                if (time != null && description != null) {
                    result.add(new ItineraryItem(time.toString(), description.toString()));
                }
            }
        }
        return result;
    }

    public java.util.List<String> mapStringList(Map<String, Object> translations, Locale locale) {
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

    /**
     * Map all itinerary translations for admin editing (es, en, pt)
     */
    public Map<String, java.util.List<ItineraryItem>> mapAllItineraryTranslations(Map<String, Object> translations) {
        if (translations == null || translations.isEmpty()) return null;
        Map<String, java.util.List<ItineraryItem>> result = new java.util.HashMap<>();
        for (String lang : translations.keySet()) {
            Object localized = translations.get(lang);
            if (localized instanceof java.util.List<?> list) {
                java.util.List<ItineraryItem> items = new java.util.ArrayList<>();
                for (Object o : list) {
                    if (o instanceof java.util.Map<?, ?> m) {
                        Object time = m.get("time");
                        Object description = m.get("description");
                        if (time != null && description != null) {
                            items.add(new ItineraryItem(time.toString(), description.toString()));
                        }
                    }
                }
                if (!items.isEmpty()) {
                    result.put(lang, items);
                }
            }
        }
        return result.isEmpty() ? null : result;
    }

    /**
     * Map all string list translations for admin editing (es, en, pt)
     */
    public Map<String, java.util.List<String>> mapAllStringListTranslations(Map<String, Object> translations) {
        if (translations == null || translations.isEmpty()) return null;
        Map<String, java.util.List<String>> result = new java.util.HashMap<>();
        for (String lang : translations.keySet()) {
            Object localized = translations.get(lang);
            if (localized instanceof java.util.List<?> list) {
                java.util.List<String> items = new java.util.ArrayList<>();
                for (Object o : list) {
                    if (o != null) {
                        items.add(o.toString());
                    }
                }
                if (!items.isEmpty()) {
                    result.put(lang, items);
                }
            }
        }
        return result.isEmpty() ? null : result;
    }


    public TourRes toTourRes(Tour tour) {
        return toTourRes(tour, Locale.forLanguageTag("es"));
    }

    public abstract List<TourRes> toTourResList(List<Tour> tours);

    // Map Media entity to TourImageRes
    @Mappings({
            @Mapping(source = "id", target = "id"),
            @Mapping(target = "imageUrl", expression = "java(generateFreshImageUrl(media))"),
            @Mapping(source = "variants", target = "variants"),
            @Mapping(source = "isHero", target = "isHeroImage"),
            @Mapping(source = "isFeatured", target = "isFeatured"),
            @Mapping(source = "displayOrder", target = "displayOrder")
    })
    public abstract TourImageRes toTourImageRes(Media media);

    /**
     * Generate a fresh signed URL for the image
     */
    protected String generateFreshImageUrl(Media media) {
        if (media == null || media.getS3Key() == null) {
            return null;
        }
        return s3StorageService.getPublicUrl(media.getS3Key());
    }

}
