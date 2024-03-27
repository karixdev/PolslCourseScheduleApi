package com.github.karixdev.scheduleservice.infrastructure.dal.repository;

import com.github.karixdev.scheduleservice.application.filter.PlanPolslDataFilter;
import com.github.karixdev.scheduleservice.application.filter.ScheduleFilter;
import com.github.karixdev.scheduleservice.application.pagination.PageRequest;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.karixdev.scheduleservice.infrastructure.dal.repository.sepcification.ScheduleEntitySpecification.*;

@Repository
public interface JpaScheduleRepository extends JpaRepository<ScheduleEntity, UUID>, JpaSpecificationExecutor<ScheduleEntity> {

    @Query("""
            SELECT schedule
            FROM Schedule schedule
            WHERE schedule.planPolslId = :planPolslId
            """)
    Optional<ScheduleEntity> findByPlanPolslId(@Param("planPolslId") Integer planPolslId);

    @Query("""
            SELECT DISTINCT schedule.major
            FROM Schedule schedule
            ORDER BY schedule.major ASC
            """)
    List<String> findUniqueMajorsOrderedAlphabetically();

    @Query("""
            SELECT DISTINCT schedule.semester
            FROM Schedule schedule
            WHERE schedule.major = :major
            ORDER BY schedule.semester ASC
            """)
    List<Integer> findSemestersByMajorOrderAsc(String major);

    @Query("""
            SELECT schedule
            FROM Schedule schedule
            WHERE schedule.major = :major
            AND schedule.semester = :semester
            """)
    List<ScheduleEntity> findByMajorAndSemester(
            @Param("major") String major,
            @Param("semester") Integer semester
    );


    default Page<ScheduleEntity> findByFilterAndPaginate(ScheduleFilter filter, PageRequest pageRequest) {
        PlanPolslDataFilter planPolslFilter = filter.planPolslDataFilter();

        Specification<ScheduleEntity> specifications = Specification.where(
                isInIds(filter.ids())
                        .and(isInMajors(filter.majors()))
                        .and(isInSemesters(filter.semesters()))
                        .and(isInGroups(filter.groups()))
                        .and(isInPlanPolslIds(planPolslFilter.ids()))
                        .and(isInPlanPolslTypes(planPolslFilter.types()))
                        .and(isInPlanPolslWeekDays(planPolslFilter.weedDays()))

        );
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size());

        return findAll(specifications, pageable);
    }

}
