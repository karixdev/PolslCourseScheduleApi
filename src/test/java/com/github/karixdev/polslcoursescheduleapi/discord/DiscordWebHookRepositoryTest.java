package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DiscordWebHookRepositoryTest extends ContainersEnvironment {
    @Autowired
    DiscordWebHookRepository underTest;

    @Autowired
    TestEntityManager em;

    @Test
    void GivenNotExistingDiscordWebHookUrl_WhenFindByUrl_ThenReturnsEmptyOptional() {
        // Given
        String url = "i-do-not-exist";

        // When
        Optional<DiscordWebHook> result = underTest.findByUrl(url);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenExistingDiscordWebHookUrl_WhenFindByUrl_ThenReturnsNotEmptyOptionalWithCorrectObject() {
        // Given
        String url = "i-exist";

        User user = em.persist(User.builder()
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_USER)
                .build());

        Schedule schedule = em.persist(Schedule.builder()
                .type(1)
                .planPolslId(2)
                .semester(1)
                .name("schedule-1")
                .groupNumber(2)
                .addedBy(user)
                .build());

        DiscordWebHook webHook = em.persistAndFlush(DiscordWebHook.builder()
                .url(url)
                .schedules(Set.of(schedule))
                .addedBy(user)
                .build());

        // When
        Optional<DiscordWebHook> result = underTest.findByUrl(url);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(webHook);
    }
}
