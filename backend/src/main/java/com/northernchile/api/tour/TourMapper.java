package com.northernchile.api.tour;

import com.northernchile.api.model.Tour;
import com.northernchile.api.model.TourImage;
import com.northernchile.api.tour.dto.TourImageRes;
import com.northernchile.api.tour.dto.TourRes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

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
            @Mapping(target = "descriptionBlocksTranslations", source = "tour.descriptionBlocksTranslations")
    })
    TourRes toTourRes(Tour tour, Locale locale);



    default TourRes toTourRes(Tour tour) {
        return toTourRes(tour, Locale.forLanguageTag("es"));
    }

    List<TourRes> toTourResList(List<Tour> tours);
 
    TourImageRes toTourImageRes(TourImage tourImage);

}
