package com.github.karixdev.polslcoursescheduleapi.fixtures;

import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleRepository;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserFixturesTest {
    @InjectMocks
    UserFixtures underTest;

    @Mock
    UserService userService;

    @Mock
    ScheduleRepository scheduleRepository;

    @Mock
    FixturesProperties fixturesProperties;

    @Test
    void shouldNotCreateAdminIfLoadFixturesIsFalse() {
        when(fixturesProperties.getLoadFixtures())
                .thenReturn(false);

        underTest.run();

        verify(userService, never())
                .createUser(
                        eq("admin@admin.com"),
                        eq("admin123"),
                        eq(UserRole.ROLE_ADMIN),
                        eq(true)
                );

    }

    @Test
    void shouldNotCreateAdminIfThereExistsOne() {
        when(fixturesProperties.getLoadFixtures())
                .thenReturn(true);

        when(userService.findByEmail(eq("admin@admin.com")))
                .thenReturn(
                        User.builder()
                                .email("admin@admin.com")
                                .password("admin123")
                                .userRole(UserRole.ROLE_ADMIN)
                                .isEnabled(true)
                                .build()
                );

        underTest.run();

        verify(userService, never())
                .createUser(
                        eq("admin@admin.com"),
                        eq("admin123"),
                        eq(UserRole.ROLE_ADMIN),
                        eq(true)
                );
    }

    @Test
    void shouldCreateUser() {
        when(fixturesProperties.getLoadFixtures())
                .thenReturn(true);

        doThrow(ResourceNotFoundException.class)
                .when(userService)
                .findByEmail(eq("admin@admin.com"));

        underTest.run();

        verify(userService)
                .createUser(
                        eq("admin@admin.com"),
                        eq("admin123"),
                        eq(UserRole.ROLE_ADMIN),
                        eq(true)
                );
    }

    @Test
    void shouldNotCreateSchedulesIfTheyAlreadyExist() {
        when(fixturesProperties.getLoadFixtures())
                .thenReturn(true);

        doThrow(ResourceNotFoundException.class)
                .when(userService)
                .findByEmail(eq("admin@admin.com"));

        User user = User.builder()
                .email("admin@admin.com")
                .password("admin123")
                .userRole(UserRole.ROLE_ADMIN)
                .isEnabled(true)
                .build();

        when(userService.createUser(any(), any(), any(), any()))
                .thenReturn(user);

        when(scheduleRepository.findByName(eq("Inf I 1/2")))
                .thenReturn(Optional.of(Schedule.builder()
                        .id(1L)
                        .type(0)
                        .planPolslId(13171)
                        .semester(1)
                        .groupNumber(1)
                        .name("Inf I 1/2")
                        .addedBy(user)
                        .build()));

        when(scheduleRepository.findByName(eq("Inf III 4/7")))
                .thenReturn(Optional.of(Schedule.builder()
                        .id(1L)
                        .type(0)
                        .planPolslId(343294803)
                        .semester(3)
                        .groupNumber(4)
                        .name("Inf I 1/2")
                        .addedBy(user)
                        .build()));

        underTest.run();

        verify(scheduleRepository, never()).save(any());
    }

    @Test
    void shouldCreateSchedules() {
        when(fixturesProperties.getLoadFixtures())
                .thenReturn(true);

        doThrow(ResourceNotFoundException.class)
                .when(userService)
                .findByEmail(eq("admin@admin.com"));

        User user = User.builder()
                .email("admin@admin.com")
                .password("admin123")
                .userRole(UserRole.ROLE_ADMIN)
                .isEnabled(true)
                .build();

        when(userService.createUser(any(), any(), any(), any()))
                .thenReturn(user);

        when(scheduleRepository.findByName(eq("Inf I 1/2")))
                .thenReturn(Optional.empty());

        when(scheduleRepository.findByName(eq("Inf III 4/7")))
                .thenReturn(Optional.empty());

        underTest.run();

        verify(scheduleRepository, times(2)).save(any());
    }
}
