package com.docintel.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIChoice {
    private OpenAIMessage message;
    @JsonProperty("content_filter_results")
    private Object contentFilterResults; // or use a proper class if needed
}
