
package com.northernchile.api.user.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class UserRes {
    private UUID id;
    private String email;
    private String fullName;
    private String role;
    private String nationality;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String authProvider;
    private Instant createdAt;

    public UserRes() {
    }

    public UserRes(UUID id, String email, String fullName, String role, String nationality, String phoneNumber, LocalDate dateOfBirth, String authProvider, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.nationality = nationality;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.authProvider = authProvider;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRes userRes = (UserRes) o;
        return Objects.equals(id, userRes.id) && Objects.equals(email, userRes.email) && Objects.equals(fullName, userRes.fullName) && Objects.equals(role, userRes.role) && Objects.equals(nationality, userRes.nationality) && Objects.equals(phoneNumber, userRes.phoneNumber) && Objects.equals(dateOfBirth, userRes.dateOfBirth) && Objects.equals(authProvider, userRes.authProvider) && Objects.equals(createdAt, userRes.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, fullName, role, nationality, phoneNumber, dateOfBirth, authProvider, createdAt);
    }

    @Override
    public String toString() {
        return "UserRes{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", nationality='" + nationality + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", authProvider='" + authProvider + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
