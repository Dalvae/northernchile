
package com.northernchile.api.user.dto;

import java.time.LocalDate;
import java.util.Objects;

public class UserUpdateReq {
    private String fullName;
    private String role;
    private String nationality;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    public UserUpdateReq() {
    }

    public UserUpdateReq(String fullName, String role, String nationality, String phoneNumber, LocalDate dateOfBirth) {
        this.fullName = fullName;
        this.role = role;
        this.nationality = nationality;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserUpdateReq that = (UserUpdateReq) o;
        return Objects.equals(fullName, that.fullName) && Objects.equals(role, that.role) && Objects.equals(nationality, that.nationality) && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(dateOfBirth, that.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, role, nationality, phoneNumber, dateOfBirth);
    }

    @Override
    public String toString() {
        return "UserUpdateReq{" +
                "fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", nationality='" + nationality + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}
