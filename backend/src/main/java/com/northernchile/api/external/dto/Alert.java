package com.northernchile.api.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert {
    @JsonProperty("sender_name")
    public String senderName;

    public String event; // Alert event name
    public long start; // Unix timestamp
    public long end; // Unix timestamp
    public String description; // Description of the alert
    public List<String> tags; // Alert tags
}
