package com.northernchile.api.booking.dto;

import lombok.Data;

@Data
public class ParticipantReq {
    private String fullName;
    private String type; // "ADULT" or "CHILD"
}
