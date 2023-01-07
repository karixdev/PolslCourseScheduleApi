package com.github.karixdev.polslcoursescheduleapi.fixtures;

import com.github.karixdev.polslcoursescheduleapi.schedule.Schedule;
import com.github.karixdev.polslcoursescheduleapi.schedule.ScheduleRepository;
import com.github.karixdev.polslcoursescheduleapi.shared.exception.ResourceNotFoundException;
import com.github.karixdev.polslcoursescheduleapi.user.User;
import com.github.karixdev.polslcoursescheduleapi.user.UserRole;
import com.github.karixdev.polslcoursescheduleapi.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataFixtures implements CommandLineRunner {
    private final UserService userService;
    private final FixturesProperties properties;
    private final ScheduleRepository scheduleRepository;

    @Override
    public void run(String... args) {
        log.info("Hello we are here!!!!!!!!!!!!!!");

        if (!properties.getLoadFixtures()) {
            return;
        }

        try {
            userService.findByEmail("admin@admin.com");
        } catch (ResourceNotFoundException e) {
            User user = userService.createUser(
                    "admin@admin.com",
                    "admin123",
                    UserRole.ROLE_ADMIN,
                    true
            );

            int[] schedulesPolslIds = {13171, 343294803};
            String[] schedulesNames = {"Inf I 1/2", "Inf III 4/7"};
            int[] schedulesGroups = {1, 4};
            int[] schedulesSemesters = {1, 3};

            for (int i = 0; i < 2; i++) {
                int polslId = schedulesPolslIds[i];
                String name = schedulesNames[i];
                int group = schedulesGroups[i];
                int semester = schedulesSemesters[i];

                if (scheduleRepository.findByName(name).isEmpty()) {
                    scheduleRepository.save(
                            Schedule.builder()
                                    .name(name)
                                    .planPolslId(polslId)
                                    .semester(semester)
                                    .groupNumber(group)
                                    .addedBy(user)
                                    .type(0)
                                    .build()
                    );
                }
            }
        }
    }
}
