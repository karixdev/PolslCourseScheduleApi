package com.github.karixdev.polslcoursescheduleapi.discord.payload.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class DiscordWebHookRequestTest {
    @Autowired
    JacksonTester<DiscordWebHookRequest> jTester;

    @Test
    void testDeserialization() throws IOException {
        String payload = """
                {
                    "url": "url",
                    "schedules_ids": [1, 1, 2, 3]
                }
                """;

        DiscordWebHookRequest result = jTester.parseObject(payload);

        assertThat(result.getUrl()).isEqualTo("url");
        assertThat(result.getSchedulesIds()).contains(1L, 2L, 3L);
    }
}
