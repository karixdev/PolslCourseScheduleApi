package com.github.karixdev.webscraperservice.application.payload;

import lombok.Builder;
import org.jsoup.nodes.Document;

@Builder
public record PlanPolslResponse(Document content) {
}
