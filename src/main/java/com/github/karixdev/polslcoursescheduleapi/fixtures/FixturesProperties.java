package com.github.karixdev.polslcoursescheduleapi.fixtures;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class FixturesProperties {
    private final Boolean loadFixtures;

    public FixturesProperties(
            @Value("${fixtures.load}")
            Boolean loadFixtures
    ) {
        this.loadFixtures = loadFixtures;
    }
}
