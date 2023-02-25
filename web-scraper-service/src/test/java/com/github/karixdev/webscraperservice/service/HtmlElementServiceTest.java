package com.github.karixdev.webscraperservice.service;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlElementServiceTest {
    HtmlElementService underTest = new HtmlElementService();

    @Test
    void GivenElementWithoutStyleAttr_WhenGetStylesAttr_ThenReturnsEmptyMap() {
        // Given
        Element el = new Element("div");

        // When
        Map<String, String> result = underTest.getStylesAttr(el);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenElementWithStyleAttr_WhenGetStylesAttr_ThenReturnsCorrectMap() {
        // Given
        Map<String, String> stylesMap = new HashMap<>();
        stylesMap.put("background-color", "blue");
        stylesMap.put("color", "white");
        stylesMap.put("padding", "10px 5px");

        Element el = new Element("div");

        StringBuilder stylesAttributeBuilder = new StringBuilder();
        stylesMap.forEach((key, value) -> stylesAttributeBuilder
                .append(key)
                .append(":")
                .append(value)
                .append(";"));
        el.attr("style", stylesAttributeBuilder.toString());

        // When
        Map<String, String> result = underTest.getStylesAttr(el);

        // Then
        assertThat(result).isEqualTo(stylesMap);
    }

}
