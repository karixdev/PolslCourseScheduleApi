package com.github.karixdev.domaincoursemapperservice.model.raw;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Link(
        @JsonProperty("text")
        String text,
        @JsonProperty("href")
        String href
) {}
