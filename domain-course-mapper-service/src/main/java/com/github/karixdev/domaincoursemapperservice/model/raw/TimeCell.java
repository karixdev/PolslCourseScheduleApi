package com.github.karixdev.domaincoursemapperservice.model.raw;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TimeCell(
        @JsonProperty("text")
        String text
) {}
