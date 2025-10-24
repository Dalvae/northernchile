package com.northernchile.api.booking.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ParticipantRes {
    private UUID id;
    private String fullName;
    private String type;
}
