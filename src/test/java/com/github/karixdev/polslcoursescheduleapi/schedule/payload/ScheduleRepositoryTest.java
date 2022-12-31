package com.github.karixdev.polslcoursescheduleapi.schedule.payload;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleRepository;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ScheduleRepositoryTest extends ContainersEnvironment {
    @Autowired
    TestEntityManager em;

    @Autowired
    ScheduleRepository underTest;

    @Test
    void GivenNotExistingScheduleName_WhenFindByName_ThenReturnsEmptyOptional() {
        // Given
        String name = "i-do-not-exist";

        // When
        Optional<Schedule> result = underTest.findByName(name);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenExistingScheduleName_WhenFindByName_ThenReturnsNotEmptyOptionalWithCorrectObject() {
        // Given
        String name = "i-do-not-exist";

        User user = em.persist(User.builder()
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_USER)
                .build());

        Schedule schedule = em.persistAndFlush(Schedule.builder()
                .type(1)
                .planPolslId(2)
                .semester(3)
                .name(name)
                .groupNumber(4)
                .addedBy(user)
                .build());

        // When
        Optional<Schedule> result = underTest.findByName(name);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(schedule);
    }
}
