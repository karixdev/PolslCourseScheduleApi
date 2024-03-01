package com.github.karixdev.scheduleservice.matcher;

import com.github.karixdev.scheduleservice.domain.entity.Schedule;
import lombok.RequiredArgsConstructor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.util.Objects;

@RequiredArgsConstructor
public class ScheduleWholeEntityArgumentMatcher implements ArgumentMatcher<Schedule> {

    private final Schedule schedule;

    public static Schedule scheduleWholeEntityEq(Schedule schedule) {
        return Mockito.argThat(new ScheduleNonIdArgumentMatcher(schedule));
    }

    @Override
    public boolean matches(Schedule other) {
        return new ScheduleNonIdArgumentMatcher(schedule).matches(other)
                && Objects.equals(schedule.getId(), other.getId());

    }

}
