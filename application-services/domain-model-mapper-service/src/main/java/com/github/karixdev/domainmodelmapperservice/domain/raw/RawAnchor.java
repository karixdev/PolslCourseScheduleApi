package com.github.karixdev.domainmodelmapperservice.domain.raw;

import lombok.Builder;

@Builder
public record RawAnchor(String address, String text) {}
