package com.github.karixdev.scheduleservice.infrastructure.dal.repository;

import com.github.karixdev.scheduleservice.application.filter.PlanPolslDataFilter;
import com.github.karixdev.scheduleservice.application.filter.ScheduleFilter;
import com.github.karixdev.scheduleservice.application.pagination.PageRequest;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import com.github.karixdev.scheduleservice.infrastructure.dal.PostgresContainerEnvironment;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

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

    @Test
    void WhenFindUniqueMajorsOrderedAlphabetically_ThenReturnsListOfCorrectlySortedStrings() {
        ScheduleEntity schedule1 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(12313)
                .semester(1)
                .major("b")
                .groupNumber(1)
                .wd(0)
                .build();

        ScheduleEntity schedule2 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(20)
                .planPolslId(1111)
                .semester(13)
                .major("c")
                .groupNumber(132)
                .wd(10)
                .build();

        ScheduleEntity schedule3 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(20)
                .planPolslId(34)
                .semester(13)
                .major("a")
                .groupNumber(132)
                .wd(10)
                .build();

        em.persistAndFlush(schedule1);
        em.persistAndFlush(schedule2);
        em.persistAndFlush(schedule3);

        // When
        List<String> result = underTest.findUniqueMajorsOrderedAlphabetically();

        // Then
        assertThat(result).containsSequence("a", "b", "c");
    }

    @Test
    void GivenMajor_WhenFindSemestersByMajorOrderAsc_ThenReturnsSemestersInCorrectOrder() {
        // Given
        String major = "major-a";

        ScheduleEntity schedule1 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(1143)
                .semester(3)
                .major(major)
                .groupNumber(1)
                .wd(0)
                .build();

        ScheduleEntity schedule2 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(20)
                .planPolslId(897876)
                .semester(1)
                .major(major)
                .groupNumber(132)
                .wd(10)
                .build();

        ScheduleEntity schedule3 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(20)
                .planPolslId(4353)
                .semester(2)
                .major(major)
                .groupNumber(132)
                .wd(10)
                .build();

        ScheduleEntity schedule4 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(20)
                .planPolslId(123123)
                .semester(13)
                .major("major-2")
                .groupNumber(132)
                .wd(10)
                .build();

        em.persistAndFlush(schedule1);
        em.persistAndFlush(schedule2);
        em.persistAndFlush(schedule3);
        em.persistAndFlush(schedule4);

        // When
        List<Integer> result = underTest.findSemestersByMajorOrderAsc(major);

        // Then
        assertThat(result).containsSequence(1, 2, 3);
    }

    @Test
    void GivenMajorAndGroup_WhenFindByMajorAndSemester_ThenReturnsListWithCorrectValues() {
        // Given
        String major = "major-a";
        int semester = 1;

        ScheduleEntity schedule1 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(1143)
                .semester(semester)
                .major(major)
                .groupNumber(1)
                .wd(0)
                .build();

        ScheduleEntity schedule2 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(20)
                .planPolslId(897876)
                .semester(2)
                .major(major)
                .groupNumber(2)
                .wd(10)
                .build();

        ScheduleEntity schedule3 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(20)
                .planPolslId(4353)
                .semester(semester)
                .major("major-b")
                .groupNumber(3)
                .wd(10)
                .build();

        em.persist(schedule1);
        em.persist(schedule2);
        em.persistAndFlush(schedule3);

        // When
        List<ScheduleEntity> result = underTest.findByMajorAndSemester(major, semester);

        // Then
        assertThat(result).containsExactly(schedule1);
    }

    @Test
    void GivenEmptyFilter_WhenFindByFilterAndPaginate_ThenReturnsAllResults() {
        // Given
        PageRequest pageRequest = new PageRequest(0, 10);
        ScheduleFilter filter = ScheduleFilter.builder()
                .planPolslDataFilter(PlanPolslDataFilter.builder().build())
                .build();

        ScheduleEntity schedule1 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(0)
                .planPolslId(1143)
                .semester(12)
                .major("major")
                .groupNumber(1)
                .wd(0)
                .build();

        ScheduleEntity schedule2 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(20)
                .planPolslId(897876)
                .semester(2)
                .major("major-b")
                .groupNumber(2)
                .wd(10)
                .build();

        em.persist(schedule1);
        em.persistAndFlush(schedule2);

        // When
        Page<ScheduleEntity> result = underTest.findByFilterAndPaginate(filter, pageRequest);

        // Then
        assertThat(result.getContent()).containsExactly(schedule1, schedule2);
    }

    @Test
    void GivenFilter_WhenFindByFilterAndPaginate_ThenReturnsEntitiesWithCorrectCriteria() {
        // Given
        PageRequest pageRequest = new PageRequest(0, 10);

        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        String major1 = "major-1";
        String major2 = "major-3";

        int semester1 = 1;
        int semester2 = 2;

        int group1 = 3;
        int group2 = 4;

        int planPolslId1 = 5;
        int planPolslId2 = 6;

        int planPolslType1 = 7;
        int planPolslType2 = 8;

        int planPolslWeekDays1 = 9;
        int planPolslWeekDays2 = 10;

        ScheduleFilter filter = ScheduleFilter.builder()
                .ids(List.of(id1, id2))
                .majors(List.of(major1, major2))
                .semesters(List.of(semester1, semester2))
                .groups(List.of(group1, group2))
                .planPolslDataFilter(
                        PlanPolslDataFilter.builder()
                                .ids(List.of(planPolslId1, planPolslId2))
                                .types(List.of(planPolslType1, planPolslType2))
                                .weedDays(List.of(planPolslWeekDays1, planPolslWeekDays2))
                                .build()
                )
                .build();

        ScheduleEntity schedule1 = ScheduleEntity.builder()
                .id(id1)
                .type(planPolslType1)
                .planPolslId(planPolslId1)
                .semester(semester1)
                .major(major1)
                .groupNumber(group1)
                .wd(planPolslWeekDays1)
                .build();

        ScheduleEntity schedule2 = ScheduleEntity.builder()
                .id(id2)
                .type(planPolslType2)
                .planPolslId(planPolslId2)
                .semester(semester2)
                .major(major2)
                .groupNumber(group2)
                .wd(planPolslWeekDays2)
                .build();

        ScheduleEntity schedule3 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(432523)
                .planPolslId(3423)
                .semester(semester2)
                .major(major2)
                .groupNumber(45)
                .wd(planPolslWeekDays2)
                .build();

        ScheduleEntity schedule4 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(432423)
                .planPolslId(32552)
                .semester(semester1)
                .major(major2)
                .groupNumber(group1)
                .wd(planPolslWeekDays2)
                .build();

        ScheduleEntity schedule5 = ScheduleEntity.builder()
                .id(UUID.randomUUID())
                .type(432423)
                .planPolslId(756765)
                .semester(432345)
                .major("major-5")
                .groupNumber(54353)
                .wd(53467)
                .build();

        em.persist(schedule1);
        em.persist(schedule2);
        em.persist(schedule3);
        em.persist(schedule4);
        em.persistAndFlush(schedule5);

        // When
        Page<ScheduleEntity> result = underTest.findByFilterAndPaginate(filter, pageRequest);

        // Then
        assertThat(result.getContent()).containsExactly(schedule1, schedule2);
    }

    @Test
    void GivenPageRequest_WhenFindByFilterAndPaginate_ThenReturnsPaginatedResults() {
        // Given
        PageRequest pageRequestFirstPage = new PageRequest(0, 5);
        PageRequest pageRequestSecondPage = new PageRequest(1, 5);

        ScheduleFilter filter = ScheduleFilter.builder()
                .planPolslDataFilter(PlanPolslDataFilter.builder().build())
                .build();

        List<ScheduleEntity> schedules = IntStream.range(0, 10).mapToObj(i ->
                ScheduleEntity.builder()
                        .id(UUID.randomUUID())
                        .type(i + 1)
                        .planPolslId(i + 1)
                        .semester(i + 1)
                        .major("major-" + i)
                        .groupNumber(i + 1)
                        .wd(i + 1)
                        .build()
        ).toList();

        schedules.forEach(em::persist);
        em.flush();

        // When
        Page<ScheduleEntity> resultFirstPage = underTest.findByFilterAndPaginate(filter, pageRequestFirstPage);
        Page<ScheduleEntity> resultSecondPage = underTest.findByFilterAndPaginate(filter, pageRequestSecondPage);

        // Then
        assertThat(resultFirstPage.getContent()).isEqualTo(schedules.subList(0, 5));
        assertThat(resultSecondPage.getContent()).isEqualTo(schedules.subList(5, 10));
    }


}