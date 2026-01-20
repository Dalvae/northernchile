package com.northernchile.api.model;

import com.northernchile.api.audit.AuditableEntity;
import com.northernchile.api.audit.AuditEntityListener;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
@EntityListeners(AuditEntityListener.class)
public class User implements AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    private String nationality;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "document_id", length = 50)
    private String documentId;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(length = 50)
    private String authProvider = "LOCAL";

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // No-argument constructor
    public User() {
    }

    // All-argument constructor (excluding auto-generated fields)
    public User(UUID id, String email, String passwordHash, String fullName, String nationality, String phoneNumber, LocalDate dateOfBirth, String documentId, String role, String authProvider, String providerId) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.nationality = nationality;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.documentId = documentId;
        this.role = role;
        this.authProvider = authProvider;
        this.providerId = providerId;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getNationality() {
        return nationality;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getRole() {
        return role;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public String getProviderId() {
        return providerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", email='" + email + "'" +
               ", passwordHash='[REDACTED]'" +
               ", fullName='" + fullName + "'" +
               ", nationality='" + nationality + "'" +
               ", phoneNumber='" + phoneNumber + "'" +
               ", dateOfBirth=" + dateOfBirth +
               ", documentId='" + documentId + "'" +
               ", role='" + role + "'" +
               ", authProvider='" + authProvider + "'" +
               ", providerId='" + providerId + "'" +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }

    // ==================== AuditableEntity Implementation ====================

    @Override
    public String getAuditDescription() {
        return email;
    }

    @Override
    public String getAuditEntityType() {
        return "USER";
    }

    @Override
    public Map<String, Object> getAuditSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("email", email);
        snapshot.put("role", role);
        snapshot.put("emailVerified", emailVerified);
        snapshot.put("deleted", deletedAt != null);
        return snapshot;
    }
}