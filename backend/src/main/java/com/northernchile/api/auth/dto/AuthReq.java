package com.northernchile.api.auth.dto;

import lombok.Data;

@Data
public class AuthReq {
    private String email;
    private String password;
}
