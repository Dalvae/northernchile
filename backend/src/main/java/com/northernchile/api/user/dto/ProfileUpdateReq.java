package com.northernchile.api.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ProfileUpdateReq(
    String fullName,
    String nationality,
    String phoneNumber,

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(type = "string", format = "date", example = "1990-05-15")
    LocalDate dateOfBirth
) {}
