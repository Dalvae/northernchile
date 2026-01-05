package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Temperature(
    double day,
    double min,
    double max,
    double night,
    double eve,
    double morn
) {}
