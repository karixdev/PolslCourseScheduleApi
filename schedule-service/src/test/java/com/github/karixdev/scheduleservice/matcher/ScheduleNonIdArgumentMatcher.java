package com.github.karixdev.scheduleservice.matcher;

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
        return Objects.equals(schedule.getSemester(), other.getSemester())
                && Objects.equals(schedule.getName(), other.getName())
                && Objects.equals(schedule.getGroupNumber(), other.getGroupNumber())
                && Objects.equals(schedule.getType(), other.getType())
                && Objects.equals(schedule.getPlanPolslId(), other.getPlanPolslId())
                && Objects.equals(schedule.getWd(), other.getWd());
    }

}
