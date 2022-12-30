package com.github.karixdev.polslcoursescheduleapi.emailverification;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public class EmailVerificationPropertiesTest extends ContainersEnvironment {
    @Autowired
    EmailVerificationProperties underTest;

    @Test
    void shouldLoadTokenExpirationHours() {
        assertThat(underTest.getTokenExpirationHours()).isNotNull();
    }

    @Test
    void shouldLoadMaxNumberOfMailsPerHour() {
        assertThat(underTest.getMaxNumberOfMailsPerHour()).isNotNull();
    }
}
