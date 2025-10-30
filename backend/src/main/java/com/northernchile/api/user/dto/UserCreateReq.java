
package com.northernchile.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;

public class UserCreateReq {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Role is required")
    private String role; // ROLE_CLIENT, ROLE_PARTNER_ADMIN, ROLE_SUPER_ADMIN

    private String nationality;
    private String phoneNumber;

    public UserCreateReq() {
    }

    public UserCreateReq(String email, String fullName, String password, String role, String nationality, String phoneNumber) {
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.role = role;
        this.nationality = nationality;
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCreateReq that = (UserCreateReq) o;
        return Objects.equals(email, that.email) && Objects.equals(fullName, that.fullName) && Objects.equals(password, that.password) && Objects.equals(role, that.role) && Objects.equals(nationality, that.nationality) && Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, fullName, password, role, nationality, phoneNumber);
    }

    @Override
    public String toString() {
        return "UserCreateReq{" +
                "email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", password='[REDACTED]'" +
                ", role='" + role + '\'' +
                ", nationality='" + nationality + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
