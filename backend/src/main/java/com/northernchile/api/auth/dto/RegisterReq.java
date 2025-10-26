
package com.northernchile.api.auth.dto;

import java.util.Objects;

public class RegisterReq {
    private String email;
    private String password;
    private String fullName;

    public RegisterReq() {
    }

    public RegisterReq(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegisterReq that = (RegisterReq) o;
        return Objects.equals(email, that.email) && Objects.equals(password, that.password) && Objects.equals(fullName, that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password, fullName);
    }

    @Override
    public String toString() {
        return "RegisterReq{" +
                "email='" + email + ''' +
                ", password='" + password + ''' +
                ", fullName='" + fullName + ''' +
                '}';
    }
}