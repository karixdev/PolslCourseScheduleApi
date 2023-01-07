package com.github.karixdev.polslcoursescheduleapi.jwt;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = NONE)
public class JwtPropertiesTest extends ContainersEnvironment {
    @Autowired
    JwtProperties underTest;

    @Test
    void shouldLoadCorrectIssuer() {
        assertThat(underTest.getIssuer())
                .isEqualTo("polsl-course-schedule-api");
    }

    @Test
    void shouldLoadCorrectTokenExpirationHours() {
        assertThat(underTest.getTokenExpirationHours())
                .isEqualTo(1);
    }

    @Test
    void shouldCreateCorrectAlgorithm() {
        assertThat(underTest.getAlgorithm().getName())
                .isEqualTo("RS256");
    }
}
