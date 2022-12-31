package com.github.karixdev.polslcoursescheduleapi.schedule.payload.response;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ScheduleResponseTest {
    @Autowired
    JacksonTester<ScheduleResponse> jTester;

    @Test
    void testSerialize() throws IOException {
        ScheduleResponse payload = new ScheduleResponse(
                1L,
                2,
                "schedule-name",
                3
        );

        var result = jTester.write(payload);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathValue("$.id")
                .isEqualTo(1);

        assertThat(result).hasJsonPath("$.semester");
        assertThat(result).extractingJsonPathValue("$.semester")
                .isEqualTo(2);

        assertThat(result).hasJsonPath("$.name");
        assertThat(result).extractingJsonPathValue("$.name")
                .isEqualTo("schedule-name");

        assertThat(result).hasJsonPath("$.group_number");
        assertThat(result).extractingJsonPathValue("$.group_number")
                .isEqualTo(3);
    }
}
