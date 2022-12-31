package com.github.karixdev.polslcoursescheduleapi.schedule.payload;

import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleRepository;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleService;
import com.github.karixdev.polslcoursescheduleapi.schedule.exception.ScheduleNameNotAvailableException;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.request.ScheduleRequest;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleResponse;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {
    @InjectMocks
    ScheduleService underTest;

    @Mock
    ScheduleRepository repository;

    Schedule schedule;

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("email@email.com")
                .password("password")
                .isEnabled(true)
                .userRole(UserRole.ROLE_ADMIN)
                .build();

        schedule = Schedule.builder()
                .id(1L)
                .type(0)
                .planPolslId(1)
                .semester(2)
                .groupNumber(3)
                .name("schedule-name")
                .addedBy(user)
                .build();
    }

    @Test
    void GivenPayloadWithUnavailableName_WhenAdd_ThenThrowsScheduleNameNotAvailableException() {
        // Given
        ScheduleRequest payload = new ScheduleRequest(
                1,
                2,
                3,
                "schedule-name",
                4
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(repository.findByName(eq("schedule-name")))
                .thenReturn(Optional.of(schedule));

        // When & Then
        assertThatThrownBy(() -> underTest.add(payload, userPrincipal))
                .isInstanceOf(ScheduleNameNotAvailableException.class)
                .hasMessage("Schedule name is not available");
    }

    @Test
    void GivenValidPayloadAndUserPrincipal_WhenAdd_ThenSavesScheduleAndReturnsCorrectScheduleResponse() {
        // Given
        ScheduleRequest payload = new ScheduleRequest(
                1,
                2,
                3,
                "schedule-name",
                4
        );
        UserPrincipal userPrincipal = new UserPrincipal(user);

        when(repository.findByName(eq("schedule-name")))
                .thenReturn(Optional.empty());

        when(repository.save(any()))
                .thenReturn(schedule);

        // When
        ScheduleResponse result = underTest.add(payload, userPrincipal);

        // Then
        assertThat(result.getId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("schedule-name");
        assertThat(result.getSemester()).isEqualTo(2);
        assertThat(result.getGroupNumber()).isEqualTo(3);

        verify(repository).save(any());
    }
}
