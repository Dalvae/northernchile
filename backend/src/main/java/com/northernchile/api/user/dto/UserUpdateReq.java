package com.northernchile.api.user.dto;

import java.time.LocalDate;

public record UserUpdateReq(
    String fullName,
    String role,
    String nationality,
    String phoneNumber,
    LocalDate dateOfBirth
) {}
