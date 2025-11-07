
package com.northernchile.api.user.dto;

import java.time.LocalDate;
import java.util.Objects;

public class ProfileUpdateReq {
    private String fullName;
    private String nationality;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    public ProfileUpdateReq() {
    }

    public ProfileUpdateReq(String fullName, String nationality, String phoneNumber, LocalDate dateOfBirth) {
        this.fullName = fullName;
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
        ProfileUpdateReq that = (ProfileUpdateReq) o;
        return Objects.equals(fullName, that.fullName) && Objects.equals(nationality, that.nationality) && Objects.equals(phoneNumber, that.phoneNumber) && Objects.equals(dateOfBirth, that.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, nationality, phoneNumber, dateOfBirth);
    }

    @Override
    public String toString() {
        return "ProfileUpdateReq{" +
                "fullName='" + fullName + '\'' +
                ", nationality='" + nationality + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                '}';
    }
}
