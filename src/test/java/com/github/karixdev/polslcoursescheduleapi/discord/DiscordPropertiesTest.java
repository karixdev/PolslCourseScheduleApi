package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
public class DiscordPropertiesTest extends ContainersEnvironment {
    @Autowired
    DiscordProperties underTest;

    @Test
    void shouldLoadWebHookBaseUrl() {
        assertThat(underTest.getWebHookBaseUrl())
                .isEqualTo("https://discord.com/api/webhooks/");
    }
}
