package com.github.karixdev.polslcoursescheduleapi.course.payload.response;

import com.github.karixdev.polslcoursescheduleapi.course.Weeks;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CourseResponseTest {
    @Autowired
    JacksonTester<CourseResponse> jTester;

    @Test
    void testSerialize() throws IOException {
        CourseResponse payload = new CourseResponse(
                1L,
                "description",
                LocalTime.of(8, 30),
                LocalTime.of(10, 0),
                DayOfWeek.FRIDAY,
                Weeks.ODD
        );

        var result = jTester.write(payload);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathValue("$.id")
                .isEqualTo(1);

        assertThat(result).hasJsonPath("$.description");
        assertThat(result).extractingJsonPathValue("$.description")
                .isEqualTo("description");

        assertThat(result).hasJsonPath("$.starts_at");
        assertThat(result).extractingJsonPathValue("$.starts_at")
                .isEqualTo("08:30:00");

        assertThat(result).hasJsonPath("$.ends_at");
        assertThat(result).extractingJsonPathValue("$.ends_at")
                .isEqualTo("10:00:00");

        assertThat(result).hasJsonPath("$.day_of_week");
        assertThat(result).extractingJsonPathValue("$.day_of_week")
                .isEqualTo("FRIDAY");

        assertThat(result).hasJsonPath("$.weeks");
        assertThat(result).extractingJsonPathValue("$.weeks")
                .isEqualTo("ODD");
    }
}
