package com.github.karixdev.polslcoursescheduleapi.fixtures;

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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserFixturesTest {
    @InjectMocks
    UserFixtures underTest;

    @Mock
    UserService userService;

    @Mock
    FixturesProperties fixturesProperties;

    @BeforeEach
    void setUp() {

    }

    @Test
    void shouldNotCreateAdminIfLoadFixturesIsFalse() {
        when(fixturesProperties.getLoadFixtures())
                .thenReturn(false);

        underTest.run(null);

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

        underTest.run(null);

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

        underTest.run(null);

        verify(userService)
                .createUser(
                        eq("admin@admin.com"),
                        eq("admin123"),
                        eq(UserRole.ROLE_ADMIN),
                        eq(true)
                );
    }
}
