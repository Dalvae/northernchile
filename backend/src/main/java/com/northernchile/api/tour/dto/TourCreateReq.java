
package com.northernchile.api.tour.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.northernchile.api.validation.ValidLanguage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TourCreateReq {
  @NotEmpty
  private Map<String, String> nameTranslations;

  @NotEmpty
  private Map<String, String> descriptionTranslations;
  private List<String> imageUrls; // NUEVO CAMPO
  @JsonProperty("isWindSensitive")
  private Boolean isWindSensitive;
  @JsonProperty("isMoonSensitive")
  private Boolean isMoonSensitive;
  @JsonProperty("isCloudSensitive")
  private Boolean isCloudSensitive; // NUEVO
  @NotNull
  private String category;

  @NotNull
  private BigDecimal price;

  @NotNull
  private Integer defaultMaxParticipants;

  @NotNull
  private Integer durationHours;

  @NotNull
  private String status;

  @NotNull
  private String contentKey;

  // Structured content fields
  private String guideName;

  @Valid
  private Map<@ValidLanguage String, @Valid List<@Valid ItineraryItem>> itineraryTranslations;

  @Valid
  private Map<@ValidLanguage String, List<String>> equipmentTranslations;

  @Valid
  private Map<@ValidLanguage String, List<String>> additionalInfoTranslations;

  public TourCreateReq() {
  }

  public TourCreateReq(Map<String, String> nameTranslations, Map<String, String> descriptionTranslations, List<String> imageUrls, Boolean isWindSensitive, Boolean isMoonSensitive, Boolean isCloudSensitive, String category, BigDecimal price,
      Integer defaultMaxParticipants, Integer durationHours, String status, String contentKey, String guideName, Map<String, List<ItineraryItem>> itineraryTranslations, Map<String, List<String>> equipmentTranslations, Map<String, List<String>> additionalInfoTranslations) {
    this.nameTranslations = nameTranslations;
    this.descriptionTranslations = descriptionTranslations;
    this.imageUrls = imageUrls;
    this.isWindSensitive = isWindSensitive;
    this.isMoonSensitive = isMoonSensitive;
    this.isCloudSensitive = isCloudSensitive;
    this.category = category;
    this.price = price;
    this.defaultMaxParticipants = defaultMaxParticipants;
    this.durationHours = durationHours;
    this.status = status;
    this.contentKey = contentKey;
    this.guideName = guideName;
    this.itineraryTranslations = itineraryTranslations;
    this.equipmentTranslations = equipmentTranslations;
    this.additionalInfoTranslations = additionalInfoTranslations;
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

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
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

  public String getContentKey() {
    return contentKey;
  }

  public void setContentKey(String contentKey) {
    this.contentKey = contentKey;
  }

  public String getGuideName() {
    return guideName;
  }

  public void setGuideName(String guideName) {
    this.guideName = guideName;
  }

  public Map<String, List<ItineraryItem>> getItineraryTranslations() {
    return itineraryTranslations;
  }

  public void setItineraryTranslations(Map<String, List<ItineraryItem>> itineraryTranslations) {
    this.itineraryTranslations = itineraryTranslations;
  }

  public Map<String, List<String>> getEquipmentTranslations() {
    return equipmentTranslations;
  }

  public void setEquipmentTranslations(Map<String, List<String>> equipmentTranslations) {
    this.equipmentTranslations = equipmentTranslations;
  }

  public Map<String, List<String>> getAdditionalInfoTranslations() {
    return additionalInfoTranslations;
  }

  public void setAdditionalInfoTranslations(Map<String, List<String>> additionalInfoTranslations) {
    this.additionalInfoTranslations = additionalInfoTranslations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TourCreateReq that = (TourCreateReq) o;
    return Objects.equals(nameTranslations, that.nameTranslations) && Objects.equals(descriptionTranslations, that.descriptionTranslations) && Objects.equals(imageUrls, that.imageUrls) && Objects.equals(isWindSensitive, that.isWindSensitive) && Objects.equals(isMoonSensitive, that.isMoonSensitive) && Objects.equals(isCloudSensitive, that.isCloudSensitive) && Objects.equals(category, that.category) && Objects.equals(price, that.price) && Objects.equals(defaultMaxParticipants, that.defaultMaxParticipants) && Objects.equals(durationHours, that.durationHours) && Objects.equals(status, that.status) && Objects.equals(contentKey, that.contentKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(nameTranslations, descriptionTranslations, imageUrls, isWindSensitive, isMoonSensitive, isCloudSensitive, category, price, defaultMaxParticipants, durationHours, status, contentKey);
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
            ", price=" + price +
            ", defaultMaxParticipants=" + defaultMaxParticipants +
            ", durationHours=" + durationHours +
            ", status='" + status + '\'' +
            ", contentKey='" + contentKey + '\'' +
            '}';
  }
}
