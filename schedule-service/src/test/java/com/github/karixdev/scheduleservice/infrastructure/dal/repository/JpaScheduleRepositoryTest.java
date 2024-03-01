package com.github.karixdev.scheduleservice.infrastructure.dal.repository;

import com.github.karixdev.scheduleservice.infrastructure.dal.PostgresContainerEnvironment;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class JpaScheduleRepositoryTest extends PostgresContainerEnvironment {

    @Autowired
    JpaScheduleRepository underTest;

    @Autowired
    TestEntityManager em;

    @Test
    void GivenPlanPolslIdOfNotExistingSchedule_WhenFindByPlanPolslId_ThenReturnsEmptyOptional() {
        // Given
        int planPolslId = 1234;

        ScheduleEntity schedule = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(1111)
                .semester(1)
                .major("major")
                .groupNumber(1)
                .wd(0)
                .build();

        em.persistAndFlush(schedule);

        // When
        Optional<ScheduleEntity> result = underTest.findByPlanPolslId(planPolslId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void GivenPlanPolslIdOfExistingSchedule_WhenFindByPlanPolslId_ThenReturnsOptionalWithCorrectEntity() {
        // Given
        int planPolslId = 1234;

        ScheduleEntity schedule = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(planPolslId)
                .semester(1)
                .major("major")
                .groupNumber(1)
                .wd(0)
                .build();

        ScheduleEntity otherSchedule = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(20)
                .planPolslId(1111)
                .semester(13)
                .major("major")
                .groupNumber(132)
                .wd(10)
                .build();

        em.persist(schedule);
        em.persistAndFlush(otherSchedule);

        // When
        Optional<ScheduleEntity> result = underTest.findByPlanPolslId(planPolslId);

        // Then
        assertThat(result).contains(schedule);
    }


}