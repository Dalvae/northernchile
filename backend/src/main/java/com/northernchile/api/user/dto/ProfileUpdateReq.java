package com.northernchile.api.user.dto;

import java.time.LocalDate;

public record ProfileUpdateReq(
    String fullName,
    String nationality,
    String phoneNumber,
    LocalDate dateOfBirth
) {}
