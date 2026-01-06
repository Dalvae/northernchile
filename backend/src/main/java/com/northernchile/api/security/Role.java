package com.northernchile.api.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    SUPER_ADMIN("ROLE_SUPER_ADMIN"),
    PARTNER_ADMIN("ROLE_PARTNER_ADMIN"),
    CLIENT("ROLE_CLIENT");

    private final String roleName;

    Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public GrantedAuthority getAuthority() {
        return new SimpleGrantedAuthority(roleName);
    }

    @Override
    public String toString() {
        return roleName;
    }
}
