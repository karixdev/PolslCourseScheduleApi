package com.github.karixdev.scheduleservice.repository;

import com.github.karixdev.scheduleservice.ContainersEnvironment;
import com.github.karixdev.scheduleservice.entity.Schedule;
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
class ScheduleRepositoryTest extends ContainersEnvironment {

    @Autowired
    ScheduleRepository underTest;

    @Autowired
    TestEntityManager em;

    @Test
    void GivenNotExistingScheduleName_WhenFindByName_ThenReturnsEmptyOptional() {
        // Given
        String name = "schedule-name";

        Schedule schedule = Schedule.builder()
                .type(0)
                .planPolslId(1111)
                .semester(1)
                .name("other-schedule")
                .groupNumber(1)
                .wd(0)
                .build();

        em.persistAndFlush(schedule);

        // When
        Optional<Schedule> result = underTest.findByName(name);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenExistingScheduleName_WhenFindByName_ThenReturnsOptionalWithProperEntity() {
        // Given
        String name = "schedule-name";

        Schedule schedule = Schedule.builder()
                .type(0)
                .planPolslId(1111)
                .semester(1)
                .name(name)
                .groupNumber(1)
                .wd(4)
                .build();

        em.persist(schedule);

        Schedule otherSchedule = Schedule.builder()
                .type(0)
                .planPolslId(1999)
                .semester(1)
                .name("other-schedule")
                .groupNumber(1)
                .wd(0)
                .build();

        em.persistAndFlush(otherSchedule);

        // When
        Optional<Schedule> result = underTest.findByName(name);

        // Then
        assertThat(result).contains(schedule);
    }

    @Test
    void WhenFindAllOrderBySemesterAndGroupNumberAsc_ThenReturnsListWithCorrectOrder() {
        Schedule schedule1 = Schedule.builder()
                .type(0)
                .planPolslId(1111)
                .semester(1)
                .name("schedule1")
                .groupNumber(2)
                .wd(0)
                .build();

        em.persist(schedule1);

        Schedule schedule2 = Schedule.builder()
                .type(0)
                .planPolslId(1999)
                .semester(1)
                .name("schedule2")
                .groupNumber(1)
                .wd(0)
                .build();

        em.persist(schedule2);

        Schedule schedule3 = Schedule.builder()
                .type(0)
                .planPolslId(1222)
                .semester(3)
                .name("schedule3")
                .groupNumber(1)
                .wd(0)
                .build();

        em.persistAndFlush(schedule3);

        // When
        List<Schedule> result =
                underTest.findAllOrderBySemesterAndGroupNumberAsc();

        // Then
        assertThat(result).hasSize(3);

        assertThat(result.get(0)).isEqualTo(schedule2);
        assertThat(result.get(1)).isEqualTo(schedule1);
        assertThat(result.get(2)).isEqualTo(schedule3);
    }

}
