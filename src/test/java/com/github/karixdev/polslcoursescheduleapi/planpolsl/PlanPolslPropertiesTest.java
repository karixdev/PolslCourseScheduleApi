package com.github.karixdev.polslcoursescheduleapi.planpolsl;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = NONE)
public class PlanPolslPropertiesTest extends ContainersEnvironment {
    @Autowired
    PlanPolslProperties underTest;

    @Test
    void shouldLoadBaseUrl() {
        assertThat(underTest.getBaseUrl())
                .isEqualTo("https://plan.polsl.pl/plan.php");
    }

    @Test
    void shouldLoadWinH() {
        assertThat(underTest.getWinH())
                .isEqualTo(1000);
    }

    @Test
    void shouldLoadWinW() {
        assertThat(underTest.getWinW())
                .isEqualTo(1000);
    }
}
