package com.github.karixdev.polslcoursescheduleapi.schedule.payload.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ScheduleRequestTest {
    @Autowired
    JacksonTester<ScheduleRequest> jTester;

    @Test
    void testDeserialize() throws IOException {
        String payload = """
                {
                    "type": 0,
                    "plan_polsl_id": 1,
                    "semester": 2,
                    "name": "schedule-name",
                    "group_number": 3
                }
                """;

        ScheduleRequest result = jTester.parseObject(payload);

        assertThat(result.getType()).isEqualTo(0);
        assertThat(result.getPlanPolslId()).isEqualTo(1);
        assertThat(result.getSemester()).isEqualTo(2);
        assertThat(result.getName()).isEqualTo("schedule-name");
        assertThat(result.getGroupNumber()).isEqualTo(3);
    }
}
