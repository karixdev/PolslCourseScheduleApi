package com.github.karixdev.scheduleservice.matcher;

import com.github.karixdev.scheduleservice.domain.entity.PlanPolslData;
import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.util.Objects;

@RequiredArgsConstructor
public class ScheduleNonIdArgumentMatcher implements ArgumentMatcher<Schedule> {

    private final Schedule schedule;

    public static Schedule scheduleNonIdEq(Schedule schedule) {
        return Mockito.argThat(new ScheduleNonIdArgumentMatcher(schedule));
    }

    @Override
    public boolean matches(Schedule other) {
        PlanPolslData planPolslData = schedule.getPlanPolslData();
        PlanPolslData otherPlanPolslData = other.getPlanPolslData();

        boolean planPolslMatch = Objects.equals(planPolslData.getId(), otherPlanPolslData.getId())
                && Objects.equals(planPolslData.getType(), otherPlanPolslData.getType())
                && Objects.equals(planPolslData.getWeekDays(), otherPlanPolslData.getWeekDays());

        return Objects.equals(schedule.getSemester(), other.getSemester())
                && Objects.equals(schedule.getMajor(), other.getMajor())
                && Objects.equals(schedule.getGroupNumber(), other.getGroupNumber())
                && planPolslMatch;
    }

}
