package com.github.karixdev.polslcoursescheduleapi.discord.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class DiscordWebHookUrlValidatorTest {
    @InjectMocks
    DiscordWebHookUrlValidator underTest;

    @Test
    void GivenInvalidUrl_WhenIsValid_ThenReturnsFalse() {
        // Given
        String url = "url";

        // When
        boolean result = underTest.isValid(url, null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void GivenValidUrl_WhenIsValid_ThenReturnsFalse() {
        // Given
        String url = "https://discord.com/api/webhooks/1231/213123";

        // When
        boolean result = underTest.isValid(url, null);

        // Then
        assertThat(result).isTrue();
    }
}
