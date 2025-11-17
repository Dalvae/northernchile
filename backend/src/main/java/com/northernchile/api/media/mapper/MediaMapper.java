package com.northernchile.api.media.mapper;

import com.northernchile.api.media.dto.MediaCreateReq;
import com.northernchile.api.media.dto.MediaRes;
import com.northernchile.api.media.model.Media;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

/**
 * MapStruct mapper for Media entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public interface MediaMapper {

    @Mappings({
            @Mapping(target = "ownerId", source = "owner.id"),
            @Mapping(target = "tourId", source = "tour.id"),
            @Mapping(target = "scheduleId", source = "schedule.id"),
            @Mapping(target = "type", expression = "java(media.getType())"),
            @Mapping(target = "displayOrder", ignore = true),  // Will be set from join table
            @Mapping(target = "isHero", ignore = true)  // Will be set from join table
    })
    MediaRes toMediaRes(Media media);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "owner", ignore = true),  // Set manually in service
            @Mapping(target = "tour", ignore = true),  // Set manually in service
            @Mapping(target = "schedule", ignore = true),  // Set manually in service
            @Mapping(target = "uploadedAt", ignore = true),  // Auto-generated
            @Mapping(target = "tourMediaAssignments", ignore = true),
            @Mapping(target = "scheduleMediaAssignments", ignore = true)
    })
    Media toMedia(MediaCreateReq req);

    /**
     * Update media metadata from request.
     * Only updates editable fields (alt, caption, tags, takenAt).
     */
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "owner", ignore = true),
            @Mapping(target = "tour", ignore = true),
            @Mapping(target = "schedule", ignore = true),
            @Mapping(target = "s3Key", ignore = true),
            @Mapping(target = "url", ignore = true),
            @Mapping(target = "sizeBytes", ignore = true),
            @Mapping(target = "contentType", ignore = true),
            @Mapping(target = "originalFilename", ignore = true),
            @Mapping(target = "variants", ignore = true),
            @Mapping(target = "exifData", ignore = true),
            @Mapping(target = "uploadedAt", ignore = true),
            @Mapping(target = "tourMediaAssignments", ignore = true),
            @Mapping(target = "scheduleMediaAssignments", ignore = true)
    })
    void updateMediaFromReq(com.northernchile.api.media.dto.MediaUpdateReq req, @MappingTarget Media media);
}
