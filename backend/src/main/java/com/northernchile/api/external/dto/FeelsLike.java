package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FeelsLike(
    double day,
    double night,
    double eve,
    double morn
) {}
