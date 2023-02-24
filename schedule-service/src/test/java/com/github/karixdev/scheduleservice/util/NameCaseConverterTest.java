package com.github.karixdev.scheduleservice.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NameCaseConverterTest {
    @Test
    void GivenString_WhenCamelToSnake_ThenReturnsCorrectString() {
        // Given
        String str = "camelCase";

        // When
        String result = NameCaseConverter.camelToSnake(str);

        // Then
        assertThat(result).isEqualTo("camel_case");
    }
}
