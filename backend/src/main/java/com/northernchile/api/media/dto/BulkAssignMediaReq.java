package com.northernchile.api.media.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for bulk assigning media to a tour or schedule.
 */
public class BulkAssignMediaReq {

    @NotNull(message = "Target ID (tour or schedule) is required")
    private UUID targetId;

    @NotEmpty(message = "At least one media ID is required")
    private List<UUID> mediaIds;

    public BulkAssignMediaReq() {
    }

    public BulkAssignMediaReq(UUID targetId, List<UUID> mediaIds) {
        this.targetId = targetId;
        this.mediaIds = mediaIds;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public void setTargetId(UUID targetId) {
        this.targetId = targetId;
    }

    public List<UUID> getMediaIds() {
        return mediaIds;
    }

    public void setMediaIds(List<UUID> mediaIds) {
        this.mediaIds = mediaIds;
    }
}
