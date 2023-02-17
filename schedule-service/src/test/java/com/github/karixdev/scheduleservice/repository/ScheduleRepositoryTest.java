package com.github.karixdev.scheduleservice.repository;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.entity.Schedule;
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
    ScheduleRepository underTest;

    @Autowired
    TestEntityManager em;

    @Test
    public void GivenNotExistingScheduleName_WhenFindByName_ThenReturnsEmptyOptional() {
        // Given
        String name = "schedule-name";

        Schedule schedule = Schedule.builder()
                .type(0)
                .planPolslId(1111)
                .semester(1)
                .name("other-schedule")
                .groupNumber(1)
                .build();

        em.persistAndFlush(schedule);

        // When
        Optional<Schedule> result = underTest.findByName(name);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void GivenExistingScheduleName_WhenFindByName_ThenReturnsOptionalWithProperEntity() {
        // Given
        String name = "schedule-name";

        Schedule schedule = Schedule.builder()
                .type(0)
                .planPolslId(1111)
                .semester(1)
                .name(name)
                .groupNumber(1)
                .build();

        em.persist(schedule);

        Schedule otherSchedule = Schedule.builder()
                .type(0)
                .planPolslId(1999)
                .semester(1)
                .name("other-schedule")
                .groupNumber(1)
                .build();

        em.persistAndFlush(otherSchedule);

        // When
        Optional<Schedule> result = underTest.findByName(name);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(schedule);
    }
}
