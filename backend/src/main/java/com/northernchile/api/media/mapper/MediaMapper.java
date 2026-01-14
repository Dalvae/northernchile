package com.northernchile.api.media.mapper;

import com.northernchile.api.media.dto.MediaCreateReq;
import com.northernchile.api.media.dto.MediaRes;
import com.northernchile.api.media.model.Media;
import com.northernchile.api.storage.S3StorageService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * MapStruct mapper for Media entity.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE)
public abstract class MediaMapper {

    protected S3StorageService s3StorageService;

    @Autowired
    public void setS3StorageService(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    @Mappings({
            @Mapping(target = "ownerId", source = "owner.id"),
            @Mapping(target = "tourId", source = "tour.id"),
            @Mapping(target = "scheduleId", source = "schedule.id"),
            @Mapping(target = "type", expression = "java(media.getType())"),
            @Mapping(target = "url", expression = "java(generateFreshUrl(media))"),
            @Mapping(target = "isInherited", ignore = true)  // Set manually in service for schedule galleries
    })
    public abstract MediaRes toMediaRes(Media media);

    /**
     * Generate a fresh presigned URL from S3 key
     */
    protected String generateFreshUrl(Media media) {
        if (media == null || media.getS3Key() == null) {
            return null;
        }
        return s3StorageService.getPublicUrl(media.getS3Key());
    }

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "owner", ignore = true),  // Set manually in service
            @Mapping(target = "tour", ignore = true),  // Set manually in service
            @Mapping(target = "schedule", ignore = true),  // Set manually in service
            @Mapping(target = "uploadedAt", ignore = true),  // Auto-generated
            @Mapping(target = "displayOrder", ignore = true),
            @Mapping(target = "isHero", ignore = true),
            @Mapping(target = "isFeatured", ignore = true)
    })
    public abstract Media toMedia(MediaCreateReq req);

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
            @Mapping(target = "displayOrder", ignore = true),
            @Mapping(target = "isHero", ignore = true),
            @Mapping(target = "isFeatured", ignore = true)
    })
    public abstract void updateMediaFromReq(com.northernchile.api.media.dto.MediaUpdateReq req, @MappingTarget Media media);
}
