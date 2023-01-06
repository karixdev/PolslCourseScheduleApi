package com.github.karixdev.polslcoursescheduleapi.fixtures;

import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserFixtures implements ApplicationRunner {
    private final UserService userService;
    private final FixturesProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.getLoadFixtures()) {
            return;
        }

        try {
            userService.findByEmail("admin@admin.com");
        } catch (ResourceNotFoundException e) {
            userService.createUser(
                    "admin@admin.com",
                    "admin123",
                    UserRole.ROLE_ADMIN,
                    true
            );
        }
    }
}
