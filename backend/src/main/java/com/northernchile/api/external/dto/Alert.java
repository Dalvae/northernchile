package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Alert(
    @JsonProperty("sender_name") String senderName,
    String event,
    long start,
    long end,
    String description,
    List<String> tags
) {}
