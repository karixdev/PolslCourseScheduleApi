package com.github.karixdev.webscraperservice.mapper;

import com.github.karixdev.webscraperservice.mapper.DOMElementAttributesMapper;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DOMElementAttributesMapperTest {

    DOMElementAttributesMapper underTest = new DOMElementAttributesMapper();

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

    @Test
    void GivenNotExistingAttributeName_WhenGetSizeAttr_ThenReturnsZero() {
        // Given
        Element el = new Element("div");
        String attrName = "cw";

        // When
        int result = underTest.getSizeAttr(el, attrName);

        // Then
        assertThat(result).isZero();
    }

    @Test
    void GivenExistingPropertyName_WhenGetSizeAttr_ThenReturnsCorrectValue() {
        // Given
        Element el = new Element("div");
        String attrName = "cw";
        el.attr(attrName, String.valueOf(10));

        // When
        int result = underTest.getSizeAttr(el, attrName);

        // Then
        assertThat(result).isEqualTo(10);
    }

    @Test
    void GivenNotExistingPropertyName_WhenGetCssSizeProperty_ThenReturnsZero() {
        // Given
        Map<String, String> styles = Map.of();
        String propertyName = "width";

        // When
        int result = underTest.getCssSizeProperty(styles, propertyName);

        // Then
        assertThat(result).isZero();
    }

    @Test
    void GivenExistingPropertyName_WhenGetCssSizeProperty_ThenReturnsCorrectValue() {
        // Given
        Map<String, String> styles = Map.of(
                "width", "10px"
        );
        String propertyName = "width";

        // When
        int result = underTest.getCssSizeProperty(styles, propertyName);

        // Then
        assertThat(result).isEqualTo(10);
    }

}
