
package com.northernchile.api.booking.dto;

import java.util.Objects;

public class ParticipantReq {
    private String fullName;
    private String documentId;
    private String nationality;
    private Integer age;
    private String pickupAddress;
    private String specialRequirements;

    public ParticipantReq() {
    }

    public ParticipantReq(String fullName, String documentId, String nationality, Integer age, String pickupAddress, String specialRequirements) {
        this.fullName = fullName;
        this.documentId = documentId;
        this.nationality = nationality;
        this.age = age;
        this.pickupAddress = pickupAddress;
        this.specialRequirements = specialRequirements;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getSpecialRequirements() {
        return specialRequirements;
    }

    public void setSpecialRequirements(String specialRequirements) {
        this.specialRequirements = specialRequirements;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantReq that = (ParticipantReq) o;
        return Objects.equals(fullName, that.fullName) && Objects.equals(type, that.type) && Objects.equals(documentId, that.documentId) && Objects.equals(nationality, that.nationality) && Objects.equals(age, that.age) && Objects.equals(pickupAddress, that.pickupAddress) && Objects.equals(specialRequirements, that.specialRequirements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, type, documentId, nationality, age, pickupAddress, specialRequirements);
    }

    @Override
    public String toString() {
        return "ParticipantReq{" +
                "fullName='" + fullName + '\'' +
                ", type='" + type + '\'' +
                ", documentId='" + documentId + '\'' +
                ", nationality='" + nationality + '\'' +
                ", age=" + age +
                ", pickupAddress='" + pickupAddress + '\'' +
                ", specialRequirements='" + specialRequirements + '\'' +
                '}';
    }
}
