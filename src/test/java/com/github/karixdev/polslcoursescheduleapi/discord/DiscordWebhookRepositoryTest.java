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
public class DiscordWebhookRepositoryTest extends ContainersEnvironment {
    @Autowired
    DiscordWebhookRepository underTest;

    @Autowired
    TestEntityManager em;

    @Test
    void GivenNotExistingDiscordWebhookUrl_WhenFindByUrl_ThenReturnsEmptyOptional() {
        // Given
        String url = "i-do-not-exist";

        // When
        Optional<DiscordWebhook> result = underTest.findByUrl(url);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenExistingDiscordWebhookUrl_WhenFindByUrl_ThenReturnsNotEmptyOptionalWithCorrectObject() {
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

        DiscordWebhook webhook = em.persistAndFlush(DiscordWebhook.builder()
                .url(url)
                .schedules(Set.of(schedule))
                .addedBy(user)
                .build());

        // When
        Optional<DiscordWebhook> result = underTest.findByUrl(url);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(webhook);
    }
}
