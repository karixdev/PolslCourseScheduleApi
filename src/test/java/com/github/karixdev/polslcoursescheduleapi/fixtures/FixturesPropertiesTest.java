package com.github.karixdev.polslcoursescheduleapi.fixtures;

import com.github.karixdev.polslcoursescheduleapi.ContainersEnvironment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringBootTest(webEnvironment = NONE)
public class FixturesPropertiesTest extends ContainersEnvironment {
    @Autowired
    FixturesProperties fixturesProperties;

    @Test
    void shouldGetProperValueOfLoadFixtures() {
        assertThat(fixturesProperties.getLoadFixtures()).isFalse();
    }
}
