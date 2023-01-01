package com.github.karixdev.polslcoursescheduleapi.frontend;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CssServiceTest {
    CssService underTest = new CssService();

    @Test
    void GivenNotExistingPropertyName_WhenGetSizePropertyValue_ThenReturnsZero() {
        // Given
        Map<String, String> styles = Map.of();
        String propertyName = "width";

        // When
        int result = underTest.getSizePropertyValue(styles, propertyName);

        // Then
        assertThat(result).isEqualTo(0);
    }

    @Test
    void GivenExistingPropertyName_WhenGetSizePropertyValue_ThenReturnsCorrectValue() {
        // Given
        Map<String, String> styles = Map.of(
                "width", "10px"
        );
        String propertyName = "width";

        // When
        int result = underTest.getSizePropertyValue(styles, propertyName);

        // Then
        assertThat(result).isEqualTo(10);
    }
}
