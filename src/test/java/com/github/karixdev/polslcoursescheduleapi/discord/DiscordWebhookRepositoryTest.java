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

import java.util.List;
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

    @Test
    void GivenUser_WhenFindByAddedBy_ThenReturnsCorrectList() {
        // Given
        User user = em.persist(User.builder()
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_USER)
                .build());

        User otherUser = em.persist(User.builder()
                .email("email-2@email.com")
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

        DiscordWebhook webhook = em.persist(DiscordWebhook.builder()
                .url("http://url-1.com")
                .schedules(Set.of(schedule))
                .addedBy(user)
                .build());

        DiscordWebhook otherWebhook = em.persistAndFlush(DiscordWebhook.builder()
                .url("http://url-2.com")
                .schedules(Set.of(schedule))
                .addedBy(otherUser)
                .build());

        // When
        List<DiscordWebhook> result = underTest.findByAddedBy(user);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(webhook.getId());
    }
}
