package com.github.karixdev.scheduleservice.infrastructure.dal.repository.sepcification;

import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity;
import com.github.karixdev.scheduleservice.infrastructure.dal.entity.ScheduleEntity_;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor(access = AccessLevel.NONE)
public class ScheduleEntitySpecification {

    public static Specification<ScheduleEntity> isInIds(List<UUID> ids) {
        return isInCollectionSpec(ScheduleEntity_.ID, ids);
    }

    public static Specification<ScheduleEntity> isInMajors(List<String> majors) {
        return isInCollectionSpec(ScheduleEntity_.MAJOR, majors);
    }

    public static Specification<ScheduleEntity> isInSemesters(List<Integer> semesters) {
        return isInCollectionSpec(ScheduleEntity_.SEMESTER, semesters);
    }

    public static Specification<ScheduleEntity> isInGroups(List<Integer> groups) {
        return isInCollectionSpec(ScheduleEntity_.GROUP_NUMBER, groups);
    }

    public static Specification<ScheduleEntity> isInPlanPolslIds(List<Integer> ids) {
        return isInCollectionSpec(ScheduleEntity_.PLAN_POLSL_ID, ids);
    }

    public static Specification<ScheduleEntity> isInPlanPolslTypes(List<Integer> types) {
        return isInCollectionSpec(ScheduleEntity_.TYPE, types);
    }

    public static Specification<ScheduleEntity> isInPlanPolslWeekDays(List<Integer> weekDays) {
        return isInCollectionSpec(ScheduleEntity_.WD, weekDays);
    }

    private static <T> Specification<ScheduleEntity> isInCollectionSpec(String column, Collection<T> collection) {
        return ((root, query, criteriaBuilder) -> {
            if (collection == null || collection.isEmpty()) {
                return null;
            }

            return root.get(column).in(collection);
        });
    }

}
