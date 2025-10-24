package com.northernchile.api.auth.dto;

import lombok.Data;

@Data
public class RegisterReq {
    private String email;
    private String password;
    private String fullName;
}
