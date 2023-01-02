package com.github.karixdev.polslcoursescheduleapi.schedule;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleRepository;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ScheduleRepositoryTest extends ContainersEnvironment {
    @Autowired
    TestEntityManager em;

    @Autowired
    ScheduleRepository underTest;

    User user;

    @BeforeEach
    void setUp() {
        user = em.persistAndFlush(User.builder()
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_USER)
                .build());
    }

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

    @Test
    void WhenFindAllOrderByGroupNumberAndSemesterDesc_ThenReturnsProperlySortedList() {
        em.persist(Schedule.builder()
                .type(1)
                .planPolslId(2)
                .semester(1)
                .name("schedule-1")
                .groupNumber(2)
                .addedBy(user)
                .build());

        em.persist(Schedule.builder()
                .type(1)
                .planPolslId(2)
                .semester(1)
                .name("schedule-2")
                .groupNumber(4)
                .addedBy(user)
                .build());

        em.persistAndFlush(Schedule.builder()
                .type(1)
                .planPolslId(2)
                .semester(2)
                .name("schedule-3")
                .groupNumber(2)
                .addedBy(user)
                .build());

        // When
        List<Schedule> result = underTest.findAllOrderByGroupNumberAndSemesterAsc();

        // Then
        assertThat(result).hasSize(3);

        Schedule schedule1 = result.get(0);
        Schedule schedule2 = result.get(1);
        Schedule schedule3 = result.get(2);

        assertThat(schedule1.getSemester())
                .isEqualTo(schedule2.getSemester());
        assertThat(schedule1.getGroupNumber())
                .isLessThan(schedule2.getGroupNumber());

        assertThat(schedule1.getSemester())
                .isLessThan(schedule3.getSemester());
    }
}
