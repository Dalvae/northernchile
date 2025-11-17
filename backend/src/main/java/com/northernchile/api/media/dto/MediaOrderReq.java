package com.northernchile.api.media.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for reordering media in a gallery.
 */
public class MediaOrderReq {

    @NotNull(message = "Media ID is required")
    private UUID mediaId;

    @NotNull(message = "Display order is required")
    private Integer displayOrder;

    public MediaOrderReq() {
    }

    public MediaOrderReq(UUID mediaId, Integer displayOrder) {
        this.mediaId = mediaId;
        this.displayOrder = displayOrder;
    }

    public UUID getMediaId() {
        return mediaId;
    }

    public void setMediaId(UUID mediaId) {
        this.mediaId = mediaId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
