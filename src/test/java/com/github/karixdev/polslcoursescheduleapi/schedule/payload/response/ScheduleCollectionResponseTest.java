package com.github.karixdev.polslcoursescheduleapi.schedule.payload.response;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ScheduleCollectionResponseTest {
    @Autowired
    JacksonTester<ScheduleCollectionResponse> jTester;

    @Test
    void testSerialization() throws IOException {
        ScheduleCollectionResponse payload =
                new ScheduleCollectionResponse(
                        Map.of(
                                1, List.of(
                                        new ScheduleResponse(
                                                1L, 1, "schedule-1", 2)),
                                2, List.of(
                                        new ScheduleResponse(
                                                2L, 2, "schedule-2", 3))
                        )
                );

        var result = jTester.write(payload);

        assertThat(result).hasJsonPath("$.semesters");

        assertThat(result).hasJsonPathArrayValue("$.semesters.1");
        assertThat(result).extractingJsonPathArrayValue("$.semesters.1")
                .hasSize(1);

        assertThat(result).hasJsonPath("$.semesters.1[0].id");
        assertThat(result).extractingJsonPathValue("$.semesters.1[0].id")
                .isEqualTo(1);
        assertThat(result).hasJsonPath("$.semesters.1[0].name");
        assertThat(result).extractingJsonPathValue("$.semesters.1[0].name")
                .isEqualTo("schedule-1");

        assertThat(result).hasJsonPath("$.semesters.1[0].group_number");
        assertThat(result).extractingJsonPathValue("$.semesters.1[0].group_number")
                .isEqualTo(2);

        assertThat(result).doesNotHaveJsonPath("$.semesters.1[0].semester");


        assertThat(result).hasJsonPathArrayValue("$.semesters.2");
        assertThat(result).extractingJsonPathArrayValue("$.semesters.2")
                .hasSize(1);

        assertThat(result).hasJsonPath("$.semesters.2[0].id");
        assertThat(result).extractingJsonPathValue("$.semesters.2[0].id")
                .isEqualTo(2);
        assertThat(result).hasJsonPath("$.semesters.2[0].name");
        assertThat(result).extractingJsonPathValue("$.semesters.2[0].name")
                .isEqualTo("schedule-2");

        assertThat(result).hasJsonPath("$.semesters.2[0].group_number");
        assertThat(result).extractingJsonPathValue("$.semesters.2[0].group_number")
                .isEqualTo(3);

        assertThat(result).doesNotHaveJsonPath("$.semesters.2[0].semester");
    }
}
