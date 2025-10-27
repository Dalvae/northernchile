
package com.northernchile.api.tour.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TourCreateReq {
  private Map<String, String> nameTranslations; // CAMBIO
  private Map<String, String> descriptionTranslations; // CAMBIO
  private List<String> imageUrls; // NUEVO CAMPO
  @JsonProperty("isWindSensitive")
  private Boolean isWindSensitive;
  @JsonProperty("isMoonSensitive")
  private Boolean isMoonSensitive;
  @JsonProperty("isCloudSensitive")
  private Boolean isCloudSensitive; // NUEVO
  private String category;
  private BigDecimal priceAdult;
  private BigDecimal priceChild;
  private Integer defaultMaxParticipants;
  private Integer durationHours;
  private String status; // <-- AÑADIR ESTA LÍNEA

  public TourCreateReq() {
  }

  public TourCreateReq(Map<String, String> nameTranslations, Map<String, String> descriptionTranslations, List<String> imageUrls, Boolean isWindSensitive, Boolean isMoonSensitive, Boolean isCloudSensitive, String category, BigDecimal priceAdult, BigDecimal priceChild,
      Integer defaultMaxParticipants, Integer durationHours, String status) {
    this.nameTranslations = nameTranslations;
    this.descriptionTranslations = descriptionTranslations;
    this.imageUrls = imageUrls;
    this.isWindSensitive = isWindSensitive;
    this.isMoonSensitive = isMoonSensitive;
    this.isCloudSensitive = isCloudSensitive;
    this.category = category;
    this.priceAdult = priceAdult;
    this.priceChild = priceChild;
    this.defaultMaxParticipants = defaultMaxParticipants;
    this.durationHours = durationHours;
    this.status = status;
  }

  public Map<String, String> getNameTranslations() {
    return nameTranslations;
  }

  public void setNameTranslations(Map<String, String> nameTranslations) {
    this.nameTranslations = nameTranslations;
  }

  public Map<String, String> getDescriptionTranslations() {
    return descriptionTranslations;
  }

  public void setDescriptionTranslations(Map<String, String> descriptionTranslations) {
    this.descriptionTranslations = descriptionTranslations;
  }

  public List<String> getImageUrls() {
    return imageUrls;
  }

  public void setImageUrls(List<String> imageUrls) {
    this.imageUrls = imageUrls;
  }

  public Boolean isWindSensitive() {
    return isWindSensitive;
  }

  public void setWindSensitive(Boolean windSensitive) {
    isWindSensitive = windSensitive;
  }

  public Boolean isMoonSensitive() {
    return isMoonSensitive;
  }

  public void setMoonSensitive(Boolean moonSensitive) {
    isMoonSensitive = moonSensitive;
  }

  public Boolean isCloudSensitive() {
    return isCloudSensitive;
  }

  public void setCloudSensitive(Boolean cloudSensitive) {
    isCloudSensitive = cloudSensitive;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public BigDecimal getPriceAdult() {
    return priceAdult;
  }

  public void setPriceAdult(BigDecimal priceAdult) {
    this.priceAdult = priceAdult;
  }

  public BigDecimal getPriceChild() {
    return priceChild;
  }

  public void setPriceChild(BigDecimal priceChild) {
    this.priceChild = priceChild;
  }

  public Integer getDefaultMaxParticipants() {
    return defaultMaxParticipants;
  }

  public void setDefaultMaxParticipants(Integer defaultMaxParticipants) {
    this.defaultMaxParticipants = defaultMaxParticipants;
  }

  public Integer getDurationHours() {
    return durationHours;
  }

  public void setDurationHours(Integer durationHours) {
    this.durationHours = durationHours;
  }

  // AÑADIR ESTOS DOS MÉTODOS
  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TourCreateReq that = (TourCreateReq) o;
    return Objects.equals(nameTranslations, that.nameTranslations) && Objects.equals(descriptionTranslations, that.descriptionTranslations) && Objects.equals(imageUrls, that.imageUrls) && Objects.equals(isWindSensitive, that.isWindSensitive) && Objects.equals(isMoonSensitive, that.isMoonSensitive) && Objects.equals(isCloudSensitive, that.isCloudSensitive) && Objects.equals(category, that.category) && Objects.equals(priceAdult, that.priceAdult) && Objects.equals(priceChild, that.priceChild) && Objects.equals(defaultMaxParticipants, that.defaultMaxParticipants) && Objects.equals(durationHours, that.durationHours) && Objects.equals(status, that.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nameTranslations, descriptionTranslations, imageUrls, isWindSensitive, isMoonSensitive, isCloudSensitive, category, priceAdult, priceChild, defaultMaxParticipants, durationHours, status);
  }

  @Override
  public String toString() {
    return "TourCreateReq{" +
            "nameTranslations=" + nameTranslations +
            ", descriptionTranslations=" + descriptionTranslations +
            ", imageUrls=" + imageUrls +
            ", isWindSensitive=" + isWindSensitive +
            ", isMoonSensitive=" + isMoonSensitive +
            ", isCloudSensitive=" + isCloudSensitive +
            ", category='" + category + '\'' +
            ", priceAdult=" + priceAdult +
            ", priceChild=" + priceChild +
            ", defaultMaxParticipants=" + defaultMaxParticipants +
            ", durationHours=" + durationHours +
            ", status='" + status + '\'' +
            '}';
  }
}
