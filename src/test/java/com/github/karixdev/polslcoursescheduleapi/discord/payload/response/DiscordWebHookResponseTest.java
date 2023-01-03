package com.github.karixdev.polslcoursescheduleapi.discord.payload.response;

import com.github.karixdev.polslcoursescheduleapi.discord.DiscordWebHook;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class DiscordWebHookResponseTest {
    @Autowired
    JacksonTester<DiscordWebHookResponse> jTester;

    @Test
    void testSerialization() throws IOException {
        User user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        DiscordWebHook discordWebHook = DiscordWebHook.builder()
                .id(1L)
                .url("url")
                .schedules(Set.of(
                        Schedule.builder()
                                .id(1L)
                                .type(0)
                                .planPolslId(1)
                                .semester(2)
                                .groupNumber(3)
                                .name("schedule-name-1")
                                .addedBy(user)
                                .build(),
                        Schedule.builder()
                                .id(2L)
                                .type(0)
                                .planPolslId(12)
                                .semester(2)
                                .groupNumber(3)
                                .name("schedule-name-2")
                                .addedBy(user)
                                .build()
                ))
                .addedBy(user)
                .build();

        DiscordWebHookResponse payload =
                new DiscordWebHookResponse(discordWebHook);

        var result = jTester.write(payload);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathValue("$.id")
                .isEqualTo(1);

        assertThat(result).hasJsonPath("$.url");
        assertThat(result).extractingJsonPathValue("$.url")
                .isEqualTo("url");

        assertThat(result).hasJsonPath("$.added_by");
        assertThat(result).extractingJsonPathValue("$.added_by.email")
                .isEqualTo("email@email.com");

        assertThat(result).doesNotHaveJsonPath("$.added_by.is_enabled");
        assertThat(result).doesNotHaveJsonPath("$.added_by.user_role");

        assertThat(result).hasJsonPath("$.schedules");
        assertThat(result).extractingJsonPathArrayValue("$.schedules")
                .hasSize(2);

        assertThat(result).hasJsonPath("$.schedules[0].id");
        assertThat(result).hasJsonPath("$.schedules[0].semester");
        assertThat(result).hasJsonPath("$.schedules[0].name");
        assertThat(result).hasJsonPath("$.schedules[0].group_number");

        assertThat(result).hasJsonPath("$.schedules[1].id");
        assertThat(result).hasJsonPath("$.schedules[1].semester");
        assertThat(result).hasJsonPath("$.schedules[1].name");
        assertThat(result).hasJsonPath("$.schedules[1].group_number");
    }
}
