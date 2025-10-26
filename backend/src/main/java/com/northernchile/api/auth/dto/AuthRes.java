
package com.northernchile.api.auth.dto;

import java.util.Objects;

public class AuthRes {
    private String token;

    public AuthRes() {
    }

    public AuthRes(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthRes authRes = (AuthRes) o;
        return Objects.equals(token, authRes.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "AuthRes{" +
                "token='" + token + ''' +
                '}';
    }
}