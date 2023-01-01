package com.github.karixdev.polslcoursescheduleapi.frontend;

import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlServiceTest {
    HtmlService underTest = new HtmlService();

    @Test
    void GivenElementWithStyles_WhenGetStyles_ThenReturnsCorrectMap() {
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
        Map<String, String> result = underTest.getStyles(el);

        // Then
        assertThat(result).isEqualTo(stylesMap);
    }

    @Test
    void GivenNotExistingAttributeName_WhenGetSizeAttributeValue_ThenReturnsZero() {
        // Given
        Element el = new Element("div");
        String attrName = "cw";

        // When
        int result = underTest.getSizeAttributeValue(el, attrName);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    void GivenExistingPropertyName_WhenGetSizePropertyValue_ThenReturnsCorrectValue() {
        // Given
        Element el = new Element("div");
        String attrName = "cw";
        el.attr(attrName, String.valueOf(10));

        // When
        int result = underTest.getSizeAttributeValue(el, attrName);

        // Then
        assertThat(result).isEqualTo(10);
    }
}
