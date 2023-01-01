package com.github.karixdev.polslcoursescheduleapi.planpolsl;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Getter
@Service
public class PlanPolslProperties {
    private final String baseUrl;
    private final Integer winW;
    private final Integer winH;

    public PlanPolslProperties(
            @Value("${plan-polsl.base-url}")
            String baseUrl,
            @Value("${plan-polsl.win-w}")
            Integer winW,
            @Value("${plan-polsl.win-h}")
            Integer winH
    ) {
        this.baseUrl = baseUrl;
        this.winW = winW;
        this.winH = winH;
    }
}
