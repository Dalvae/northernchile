
package com.northernchile.api.auth.dto;

import java.util.Objects;

public class AuthReq {
    private String email;
    private String password;

    public AuthReq() {
    }

    public AuthReq(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthReq authReq = (AuthReq) o;
        return Objects.equals(email, authReq.email) && Objects.equals(password, authReq.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }

    @Override
    public String toString() {
        return "AuthReq{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
