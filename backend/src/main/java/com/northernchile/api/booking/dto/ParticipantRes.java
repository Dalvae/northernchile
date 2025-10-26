
package com.northernchile.api.booking.dto;

import java.util.Objects;
import java.util.UUID;

public class ParticipantRes {
    private UUID id;
    private String fullName;
    private String type;

    public ParticipantRes() {
    }

    public ParticipantRes(UUID id, String fullName, String type) {
        this.id = id;
        this.fullName = fullName;
        this.type = type;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
        ParticipantRes that = (ParticipantRes) o;
        return Objects.equals(id, that.id) && Objects.equals(fullName, that.fullName) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fullName, type);
    }

    @Override
    public String toString() {
        return "ParticipantRes{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
