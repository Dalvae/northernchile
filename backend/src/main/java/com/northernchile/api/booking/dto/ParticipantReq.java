
package com.northernchile.api.booking.dto;

import java.util.Objects;

public class ParticipantReq {
    private String fullName;
    private String type; // "ADULT" or "CHILD"

    public ParticipantReq() {
    }

    public ParticipantReq(String fullName, String type) {
        this.fullName = fullName;
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParticipantReq that = (ParticipantReq) o;
        return Objects.equals(fullName, that.fullName) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, type);
    }

    @Override
    public String toString() {
        return "ParticipantReq{" +
                "fullName='" + fullName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
